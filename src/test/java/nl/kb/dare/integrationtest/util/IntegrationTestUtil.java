package nl.kb.dare.integrationtest.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class IntegrationTestUtil {
    public static final String APP_HOST = "localhost:4567";
    public static final String APP_URL = "http://" + APP_HOST;
    public static final String APP_ADMIN_URL = "http://localhost:4568";


    public static String getRepositoryPayload(String filename) throws IOException {
        return IOUtils.toString(IntegrationTestUtil.class.getResourceAsStream(filename), "UTF-8");
    }
}
