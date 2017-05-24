package nl.kb.http.mockserver;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;

public class MockServer extends Application<MockConfig> {


    @Override
    public void run(MockConfig mockConfig, Environment environment) throws Exception {

        register(environment, new TimeoutEndpoint());
    }

    private void register(Environment environment, Object component) {
        environment.jersey().register(component);
    }
}
