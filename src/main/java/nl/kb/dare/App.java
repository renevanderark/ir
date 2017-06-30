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
import nl.kb.dare.databasetasks.LoadOracleSchemaTask;
import nl.kb.dare.databasetasks.LoadRepositoriesTask;
import nl.kb.dare.endpoints.AuthenticationEndpoint;
import nl.kb.dare.endpoints.HarvesterEndpoint;
import nl.kb.dare.endpoints.OaiRecordFetcherEndpoint;
import nl.kb.dare.endpoints.RecordEndpoint;
import nl.kb.dare.endpoints.RecordStatusEndpoint;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.endpoints.kbaut.KbAuthFilter;
import nl.kb.dare.mail.mailer.StubbedMailer;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.nbn.NumbersController;
import nl.kb.dare.scheduledjobs.DailyIdentifierHarvestScheduler;
import nl.kb.dare.scheduledjobs.IdentifierHarvesterDaemon;
import nl.kb.dare.scheduledjobs.ObjectHarvesterDaemon;
import nl.kb.dare.websocket.SocketNotifier;
import nl.kb.filestorage.FileStorage;
import nl.kb.http.HttpFetcher;
import nl.kb.http.LenientHttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.xslt.PipedXsltTransformer;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import javax.xml.transform.stream.StreamSource;

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
        final DBIFactory factory = new DBIFactory();
        final DBI db = factory.build(environment, config.getDataSourceFactory(), "datasource");

        // Fault tolerant HTTP GET clients
        final HttpFetcher httpFetcherForIdentifierHarvest = new LenientHttpFetcher(true);
        final HttpFetcher httpFetcherForObjectHarvest = new LenientHttpFetcher(false);
        final HttpFetcher httpFetcherForNumberGenerator = new LenientHttpFetcher(false);

        // Handler factory for responses from httpFetcher
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();


        // Data access objects
        final RepositoryDao repositoryDao = db.onDemand(RepositoryDao.class);
        final RecordDao recordDao = db.onDemand(RecordDao.class);
        final ErrorReportDao errorReportDao = db.onDemand(ErrorReportDao.class);

        // File storage access
        final FileStorageFactory fileStorageFactory = config.getFileStorageFactory();
        if (fileStorageFactory == null) {
            throw new IllegalStateException("No file storage configuration provided");
        }
        final FileStorage fileStorage = fileStorageFactory.getFileStorage();


        // Handler for websocket broadcasts to the browser
        final SocketNotifier socketNotifier = new SocketNotifier();


        // Generates database aggregation of record (publication) statuses
        final RecordReporter recordReporter = new RecordReporter(db);
        // Generates database aggregation of reported errors
        final ErrorReporter errorReporter = new ErrorReporter(db);


        // Data transfer controllers
        // Fetches track numbers for new records using the number generator service
        final NumbersController numbersController = new NumbersController(
                config.getNumbersEndpoint(), httpFetcherForNumberGenerator, responseHandlerFactory);
        // Stores harvest states for the repositories in the database
        final RepositoryController repositoryController = new RepositoryController(repositoryDao, socketNotifier);
        // Stores batches of new records and updates ~oai deleted~ existing records in the database
        final RecordBatchLoader recordBatchLoader = new RecordBatchLoader(recordDao, numbersController, recordReporter, socketNotifier);

        // Xslt processors
        final StreamSource stripOaiXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/strip_oai_wrapper.xsl"));
        final StreamSource didlToManifestXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/didl-to-manifest.xsl"));

        final PipedXsltTransformer xsltTransformer = PipedXsltTransformer.newInstance(stripOaiXslt, didlToManifestXslt);

        // Process that manages the amount of running identifier harvesters every 200ms
        final IdentifierHarvesterDaemon identifierHarvesterDaemon = new IdentifierHarvesterDaemon(
                repositoryController,
                recordBatchLoader,
                httpFetcherForIdentifierHarvest,
                responseHandlerFactory,
                repositoryDao,
                socketNotifier,
                config.getMailerFactory() == null ? new StubbedMailer() : config.getMailerFactory().getMailer(),
                config.getMaxParallelHarvests()
        );

        // Initialize wrapped services (injected in endpoints)

        // Validator for OAI/PMH settings of a repository
        final RepositoryValidator repositoryValidator = new RepositoryValidator(httpFetcherForIdentifierHarvest, responseHandlerFactory);

        // Process that starts publication downloads every n miliseconds
        final ObjectHarvesterDaemon objectHarvesterDaemon = new ObjectHarvesterDaemon(
                recordDao,
                repositoryDao,
                httpFetcherForObjectHarvest,
                responseHandlerFactory,
                fileStorage,
                xsltTransformer,
                socketNotifier,
                recordReporter,
                errorReportDao,
                errorReporter,
                config.getMaxParallelDownloads(),
                config.getDownloadQueueFillDelayMs());

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
        register(environment, new RecordEndpoint(filter, recordDao, errorReportDao, fileStorage));

        // Operational controls for repository harvesters
        register(environment, new HarvesterEndpoint(filter, repositoryDao, identifierHarvesterDaemon));

        // Operational controls for record fetcher
        register(environment, new OaiRecordFetcherEndpoint(filter, objectHarvesterDaemon));

        // Record status endpoint
        register(environment, new RecordStatusEndpoint(filter, recordReporter, errorReporter));

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
