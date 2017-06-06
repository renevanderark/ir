package nl.kb.dare;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import nl.kb.dare.endpoints.HarvesterEndpoint;
import nl.kb.dare.endpoints.OaiRecordFetcherEndpoint;
import nl.kb.dare.endpoints.RecordStatusEndpoint;
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.jobs.ScheduledHarvestRunner;
import nl.kb.dare.jobs.ScheduledOaiRecordFetcher;
import nl.kb.dare.jobs.ScheduledRepositoryHarvester;
import nl.kb.dare.model.SocketNotifier;
import nl.kb.dare.model.preproces.RecordBatchLoader;
import nl.kb.dare.model.preproces.RecordDao;
import nl.kb.dare.model.preproces.RecordReporter;
import nl.kb.dare.model.reporting.ErrorReportDao;
import nl.kb.dare.model.reporting.ErrorReporter;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.model.repository.oracle.OracleRepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.dare.nbn.NumbersController;
import nl.kb.dare.taskmanagers.ManagedPeriodicTask;
import nl.kb.dare.tasks.LoadOracleSchemaTask;
import nl.kb.dare.tasks.LoadRepositoriesTask;
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
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets"));

        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {
        final DBIFactory factory = new DBIFactory();
        final DBI db = factory.build(environment, config.getDataSourceFactory(), "datasource");

        // GET request class for harvesting
        final HttpFetcher httpFetcher = new LenientHttpFetcher(true);
        final HttpFetcher downloader = new LenientHttpFetcher(false);
        final HttpFetcher numbersGetter = new LenientHttpFetcher(false);

        // Handler factory for responses from httpFetcher
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();


        // Data access objects
        final RepositoryDao repositoryDao = config.getDatabaseProvider().equals("oracle")
                ? db.onDemand(OracleRepositoryDao.class)
                : db.onDemand(RepositoryDao.class);

        final RecordDao recordDao = db.onDemand(RecordDao.class);

        final ErrorReportDao errorReportDao = db.onDemand(ErrorReportDao.class);

        // File storage access
        final FileStorage fileStorage = config.getFileStorageFactory().getFileStorage();


        // Cross process exchange utilities (reporters and notifiers)
        final SocketNotifier socketNotifier = new SocketNotifier();
        final RecordReporter recordReporter = new RecordReporter(db);
        final ErrorReporter errorReporter = new ErrorReporter(db);


        // Data mutation controllers
        final NumbersController numbersController = new NumbersController(config.getNumbersEndpoint(), numbersGetter,
                responseHandlerFactory);

        final RepositoryController repositoryController = new RepositoryController(repositoryDao, socketNotifier);
        final RecordBatchLoader recordBatchLoader = new RecordBatchLoader(recordDao, numbersController, recordReporter, socketNotifier);

        // Xslt processors
        final StreamSource stripOaiXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/strip_oai_wrapper.xsl"));
        final StreamSource didlToManifestXslt = new StreamSource(PipedXsltTransformer.class.getResourceAsStream("/xslt/didl-to-manifest.xsl"));

        final PipedXsltTransformer xsltTransformer = PipedXsltTransformer.newInstance(stripOaiXslt, didlToManifestXslt);

        // Process that manages the amount of running harvesters every 200ms
        final ScheduledHarvestRunner harvestRunner = new ScheduledHarvestRunner(
                repositoryController,
                recordBatchLoader,
                httpFetcher,
                responseHandlerFactory,
                repositoryDao,
                socketNotifier,
                config.getMaxParallelHarvests()
        );

        // Initialize wrapped services (injected in endpoints)
        final RepositoryValidator repositoryValidator = new RepositoryValidator(httpFetcher, responseHandlerFactory);

        // Process that starts record downloads every 200ms
        final ScheduledOaiRecordFetcher recordFetcher = new ScheduledOaiRecordFetcher(
                recordDao,
                repositoryDao,
                downloader,
                responseHandlerFactory,
                fileStorage,
                xsltTransformer,
                socketNotifier,
                recordReporter,
                errorReportDao,
                errorReporter
        );

        // Fix potential data problems caused by hard termination of application
        try {
            // Reset all records which have PROCESSING state to PENDING
            recordDao.fetchAllByProcessStatus(ProcessStatus.PROCESSING.getCode()).forEach(record -> {
                record.setState(ProcessStatus.PENDING);
                recordDao.updateState(record);
            });
        } catch (Exception e) {
            LOG.error("Failed to fix data on boot, probably caused by missing schema", e);
        }

        // Register endpoints

        // CRUD operations for repositories (harvest definitions)
        register(environment, new RepositoriesEndpoint(repositoryDao, repositoryValidator, repositoryController));

        // Operational controls for repository harvesters
        register(environment, new HarvesterEndpoint(repositoryDao, harvestRunner));

        // Operational controls for record fetcher
        register(environment, new OaiRecordFetcherEndpoint(recordFetcher));


        // Record status endpoint
        register(environment, new RecordStatusEndpoint(recordReporter, errorReporter));

        // HTML + javascript app
        register(environment, new RootEndpoint(config.getHostName()));

        // Make JsonProcessingException show details
        register(environment, new JsonProcessingExceptionMapper(true));


        // Websocket servlet status update notifier
        registerServlet(environment, new StatusWebsocketServlet(), "statusWebsocket");

        // Lifecycle (scheduled tasks/deamons)
        // Process that starts record downloads every 200ms
        environment.lifecycle().manage(new ManagedPeriodicTask(recordFetcher));

        // Process that manages the amount of running harvesters every 200ms
        environment.lifecycle().manage(new ManagedPeriodicTask(harvestRunner));

        // Process that starts harvests daily, weekly or monthly
        environment.lifecycle().manage(new ManagedPeriodicTask(new ScheduledRepositoryHarvester(
                repositoryDao,
                harvestRunner
        )));


        // Task endpoints
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
