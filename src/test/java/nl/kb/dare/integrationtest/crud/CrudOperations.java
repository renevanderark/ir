package nl.kb.dare.integrationtest.crud;

import nl.kb.dare.integrationtest.IntegrationTestUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class CrudOperations {

    public static void createH2Schema() throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(String.format("%s/tasks/create-h2-schema", IntegrationTestUtil.APP_ADMIN_URL));
        httpClient.execute(httpPost);

    }

    public static String createRepository(String payload) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(String.format("%s/repositories", IntegrationTestUtil.APP_URL));
        httpPost.addHeader("Content-Type", "application/json");
        httpPost.addHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(payload));
        final HttpResponse httpResponse = httpClient.execute(httpPost);

        return httpResponse.getFirstHeader("Location").getValue();

    }

}
