package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.kb.dare.config.FileStorageFactory;
import nl.kb.dare.config.FileStorageGoal;
import nl.kb.dare.databasetasks.LoadOracleSchemaTask;
import nl.kb.dare.databasetasks.LoadRepositoriesTask;
import nl.kb.dare.endpoints.AuthenticationEndpoint;
import nl.kb.dare.endpoints.HarvesterEndpoint;
import nl.kb.dare.endpoints.ObjectHarvesterEndpoint;
import nl.kb.dare.endpoints.RecordEndpoint;
import nl.kb.dare.endpoints.RecordStatusEndpoint;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.identifierharvester.IdentifierHarvestErrorFlowHandler;
import nl.kb.dare.identifierharvester.IdentifierHarvester;
import nl.kb.dare.idgen.IdGenerator;
import nl.kb.dare.idgen.uuid.UUIDGenerator;
import nl.kb.dare.mail.Mailer;
import nl.kb.dare.mail.mailer.StubbedMailer;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.reporting.ExcelReportBuilder;
import nl.kb.dare.model.reporting.ExcelReportDao;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.objectharvester.ObjectHarvestErrorFlowHandler;
import nl.kb.dare.objectharvester.ObjectHarvester;
import nl.kb.dare.objectharvester.ObjectHarvesterOperations;
import nl.kb.dare.objectharvester.ObjectHarvesterResourceOperations;
import nl.kb.dare.scheduledjobs.DailyIdentifierHarvestScheduler;
import nl.kb.dare.scheduledjobs.IdentifierHarvestSchedulerDaemon;
import nl.kb.dare.scheduledjobs.ObjectHarvestSchedulerDaemon;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.filestorage.FileStorage;
import nl.kb.http.HttpFetcher;
import nl.kb.http.LenientHttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.manifest.ManifestFinalizer;
import nl.kb.xslt.PipedXsltTransformer;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.xml.transform.stream.StreamSource;
import java.util.Map;

public class App extends Application<Config> {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws Exception {
        new App().run(args);
    }

    @Override
    public void initialize(Bootstrap<Config> bootstrap) {
        // Serve static files
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets"));

        // Support ENV variables in configuration yaml files.
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        final Mailer mailer = config.getMailerFactory() == null ?
                new StubbedMailer() : config.getMailerFactory().getMailer();

        final DBIFactory factory = new DBIFactory();
        final DBI db = factory.build(environment, config.getDataSourceFactory(), "datasource");

        // Fault tolerant HTTP GET clients
        final HttpFetcher httpFetcherForIdentifierHarvest = new LenientHttpFetcher(true);
        final HttpFetcher httpFetcherForObjectHarvest = new LenientHttpFetcher(false);

        // Handler factory for responses from httpFetcher
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();


        // Data access objects
        final RepositoryDao repositoryDao = db.onDemand(RepositoryDao.class);
        final RecordDao recordDao = db.onDemand(RecordDao.class);
        final ErrorReportDao errorReportDao = db.onDemand(ErrorReportDao.class);
        final ExcelReportDao excelReportDao = db.onDemand(ExcelReportDao.class);

        // File storage access
        final Map<FileStorageGoal, FileStorageFactory> fileStorageFactories = config.getFileStorageFactory();
        if (fileStorageFactories == null) {
            throw new IllegalStateException("No file storage configuration provided");
        }
        if (fileStorageFactories.get(FileStorageGoal.PROCESSING) == null) {
            throw new IllegalStateException("No file storage location provided for 'processing'");
        }
        if (fileStorageFactories.get(FileStorageGoal.DONE) == null) {
            throw new IllegalStateException("No file storage location provided for 'done'");
        }
        if (fileStorageFactories.get(FileStorageGoal.REJECTED) == null) {
            throw new IllegalStateException("No file storage location provided for 'rejected'");
        }

        final FileStorage processingStorage = fileStorageFactories.get(FileStorageGoal.PROCESSING).getFileStorage();
        final FileStorage doneStorage = fileStorageFactories.get(FileStorageGoal.DONE).getFileStorage();
        final FileStorage rejectedStorage = fileStorageFactories.get(FileStorageGoal.REJECTED).getFileStorage();

        // Handler for websocket broadcasts to the browser
        final SocketNotifier socketNotifier = new SocketNotifier();


        // Generates database aggregation of record (publication) statuses
        final RecordReporter recordReporter = new RecordReporter(db);
        // Generates database aggregation of reported errors
        final ErrorReporter errorReporter = new ErrorReporter(db);


        // Data transfer controllers
        // Fetches track numbers for new records using the number generator service
        final IdGenerator idGenerator = new UUIDGenerator();
        // Stores harvest states for the repositories in the database
        final RepositoryController repositoryController = new RepositoryController(repositoryDao, socketNotifier);
        // Stores batches of new records and updates ~oai deleted~ existing records in the database
        final RecordBatchLoader recordBatchLoader = new RecordBatchLoader(
                recordDao, repositoryDao, idGenerator, recordReporter, socketNotifier,
                config.getBatchLoadSampleMode());
        // Handler for errors in services the IdentfierHarvesters depend on (numbers endpoint; oai endpoint)
        final IdentifierHarvestErrorFlowHandler identifierHarvestErrorFlowHandler =
                new IdentifierHarvestErrorFlowHandler(repositoryController, mailer);


        // Xslt processors
        final StreamSource stripOaiXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/strip_oai_wrapper.xsl"));
        final StreamSource didlToManifestXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/didl-to-pd.xsl"));

        final PipedXsltTransformer xsltTransformer = PipedXsltTransformer.newInstance(stripOaiXslt, didlToManifestXslt);

        // Builder for new instances of identifier harvesters
        final IdentifierHarvester.Builder harvesterBuilder = new IdentifierHarvester.Builder(repositoryController,
                recordBatchLoader, httpFetcherForIdentifierHarvest, responseHandlerFactory, repositoryDao);

        // Process that manages the amount of running identifier harvesters every 200ms
        final IdentifierHarvestSchedulerDaemon identifierHarvesterDaemon = new IdentifierHarvestSchedulerDaemon(
                harvesterBuilder,
                socketNotifier,
                identifierHarvestErrorFlowHandler,
                config.getMaxParallelHarvests()
        );

        // Helper classes for the ObjectHarvester
        // handles downloads of resources
        final ObjectHarvesterResourceOperations objectHarvesterResourceOperations =
                new ObjectHarvesterResourceOperations(httpFetcherForObjectHarvest, responseHandlerFactory);

        // Organises the operations of downloading a full publication object
        final ObjectHarvesterOperations objectHarvesterOperations = new ObjectHarvesterOperations(
                processingStorage, rejectedStorage, doneStorage,
                httpFetcherForObjectHarvest, responseHandlerFactory, xsltTransformer,
                objectHarvesterResourceOperations, new ManifestFinalizer());

        // Handles expected failure flow (exceed maximum consecutive download failures
        final ObjectHarvestErrorFlowHandler objectHarvestErrorFlowHandler = new ObjectHarvestErrorFlowHandler(
                repositoryController, repositoryDao, mailer);

        // The object harvester
        final ObjectHarvester objectHarvester = new ObjectHarvester.Builder()
                .setRepositoryDao(repositoryDao)
                .setRecordDao(recordDao)
                .setErrorReportDao(errorReportDao)
                .setObjectHarvesterOperations(objectHarvesterOperations)
                .setRecordReporter(recordReporter)
                .setErrorReporter(errorReporter)
                .setSocketNotifier(socketNotifier)
                .setMaxSequentialDownloadFailures(config.getMaxConsecutiveDownloadFailures())
                .setObjectHarvestErrorFlowHandler(objectHarvestErrorFlowHandler)
                .setHarvesterVersion(config.getHarvesterVersion())
                .create();

        // Initialize wrapped services (injected in endpoints)

        // Process that starts publication downloads every n miliseconds
        final ObjectHarvestSchedulerDaemon objectHarvesterDaemon = new ObjectHarvestSchedulerDaemon(
                objectHarvester,
                socketNotifier,
                config.getMaxParallelDownloads(),
                config.getDownloadQueueFillDelayMs()
        );

        // Validator for OAI/PMH settings of a repository
        final RepositoryValidator repositoryValidator = new RepositoryValidator(httpFetcherForIdentifierHarvest, responseHandlerFactory);

        // Fix potential data problems caused by hard termination of application
        try {
            // Reset all records which have PROCESSING state to PENDING
            recordDao.fetchAllByProcessStatus(ProcessStatus.PROCESSING.getCode()).forEachRemaining(record -> {
                record.setState(ProcessStatus.PENDING);
                recordDao.updateState(record);
            });
        } catch (Exception e) {
            LOG.warn("Failed to fix data on boot, probably caused by missing schema", e);
        }

        final KbAuthFilter filter = new KbAuthFilter(config.getAuthEnabled());

        // Register endpoints

        // Authentication services
        register(environment, new AuthenticationEndpoint(filter, config.getKbAutLocation(), config.getHostName()));

        // CRUD operations for repositories (harvest definitions)
        register(environment, new RepositoriesEndpoint(filter, repositoryDao, repositoryValidator, repositoryController));

        // Read operations for records (find, view, download)
        register(environment, new RecordEndpoint(filter, recordDao, errorReportDao, recordReporter,
                socketNotifier));

        // Operational controls for repository harvesters
        register(environment, new HarvesterEndpoint(filter, repositoryDao, identifierHarvesterDaemon));

        // Operational controls for record fetcher
        register(environment, new ObjectHarvesterEndpoint(filter, objectHarvesterDaemon));

        // Record status endpoint
        register(environment, new RecordStatusEndpoint(filter, recordReporter, errorReporter, excelReportDao,
            new ExcelReportBuilder()));

        // HTML + javascript app
        register(environment, new RootEndpoint(config.getKbAutLocation(), config.getHostName()));

        // Make JsonProcessingException show details
        register(environment, new JsonProcessingExceptionMapper(true));

        // Websocket servlet status update notifier
        registerServlet(environment, new StatusWebsocketServlet(), "statusWebsocket");

        // Lifecycle (scheduled databasetasks/deamons)
        // Process that starts publication downloads every 200ms
        environment.lifecycle().manage(new ManagedPeriodicTask(objectHarvesterDaemon));

        // Process that manages the amount of running identifier harvesters every 200ms
        environment.lifecycle().manage(new ManagedPeriodicTask(identifierHarvesterDaemon));

        // Process that starts harvests daily, weekly or monthly
        environment.lifecycle().manage(new ManagedPeriodicTask(new DailyIdentifierHarvestScheduler(
                repositoryDao,
                identifierHarvesterDaemon
        )));


        // Database task endpoints
        environment.admin().addTask(new LoadOracleSchemaTask(db));
        environment.admin().addTask(new LoadRepositoriesTask(repositoryDao));
    }


    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }

    private void registerServlet(Environment environment, Servlet servlet, String name) {
        environment.servlets().addServlet(name, servlet).addMapping("/status-socket");
    }
}
