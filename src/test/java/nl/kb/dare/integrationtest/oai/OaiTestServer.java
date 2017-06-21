package nl.kb.dare.integrationtest.oai;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import nl.kb.dare.integrationtest.oai.endpoints.OaiEndpoint;


public class OaiTestServer extends Application<Config> {

    public static void main(String... args) throws Exception {
        new OaiTestServer().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

        register(environment, new OaiEndpoint());
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
