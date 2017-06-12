package nl.kb.dare.integrationtest;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class IntegrationTestUtil {
    public static final String APP_HOST = "localhost:4567";
    public static final String APP_URL = "http://" + APP_HOST;
    public static final String APP_ADMIN_URL = "http://localhost:4568";


    static String getRepositoryPayload() throws IOException {
        return IOUtils.toString(IntegrationTestUtil.class.getResourceAsStream("/integrationtest/payloads/repository.json"), "UTF-8");
    }
}
