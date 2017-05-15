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
import nl.kb.dare.endpoints.RepositoriesEndpoint;
import nl.kb.dare.endpoints.RootEndpoint;
import nl.kb.dare.endpoints.StatusWebsocketServlet;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.repository.RepositoryNotifier;
import nl.kb.dare.model.repository.RepositoryValidator;
import nl.kb.dare.model.repository.oracle.OracleRepositoryDao;
import nl.kb.dare.tasks.LoadOracleSchemaTask;
import nl.kb.dare.tasks.LoadRepositoriesTask;
import nl.kb.http.HttpFetcher;
import nl.kb.http.LenientHttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import org.skife.jdbi.v2.DBI;

import javax.servlet.Servlet;

public class App extends Application<Config> {
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
        // Handler factory for responses from httpFetcher
        final ResponseHandlerFactory responseHandlerFactory = new ResponseHandlerFactory();


        // Data access objects
        final RepositoryDao repositoryDao = config.getDatabaseProvider().equals("oracle")
                ? db.onDemand(OracleRepositoryDao.class)
                : db.onDemand(RepositoryDao.class);

        // Cross process exchange utilities (notifiers)
        final RepositoryNotifier repositoryNotifier = new RepositoryNotifier(repositoryDao);


        // Initialize wrapped services (injected in endpoints)
        final RepositoryValidator repositoryValidator = new RepositoryValidator(httpFetcher, responseHandlerFactory);


        // Register endpoints

        // CRUD operations for repositories (harvest definitions)
        register(environment, new RepositoriesEndpoint(repositoryDao, repositoryValidator, repositoryNotifier));

        // Operational controls for repository harvesters
        register(environment, new HarvesterEndpoint(repositoryDao, repositoryNotifier, httpFetcher, responseHandlerFactory));

        // HTML + javascript app
        register(environment, new RootEndpoint(config.getHostName()));

        // Make JsonProcessingException show details
        register(environment, new JsonProcessingExceptionMapper(true));


        // Websocket servlet status update notifier
        registerServlet(environment, new StatusWebsocketServlet(), "statusWebsocket");

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
