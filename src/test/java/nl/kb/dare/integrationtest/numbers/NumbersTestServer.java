package nl.kb.dare.integrationtest.numbers;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import nl.kb.dare.integrationtest.numbers.endpoints.NumbersEndPoint;


public class NumbersTestServer extends Application<Config> {

    public static void main(String... args) throws Exception {
        new NumbersTestServer().run(args);
    }

    @Override
    public void run(Config config, Environment environment) throws Exception {

        register(environment, new NumbersEndPoint());
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
