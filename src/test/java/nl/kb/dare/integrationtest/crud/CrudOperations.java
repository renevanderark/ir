package nl.kb.dare.integrationtest.crud;

import nl.kb.dare.integrationtest.IntegrationTestUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
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

    public static int enableRepository(Integer repositoryId) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();

        final HttpPut httpPut = new HttpPut(String.format("%s/repositories/%d/enable",
                IntegrationTestUtil.APP_URL, repositoryId));

        final HttpResponse response = httpClient.execute(httpPut);
        return response.getStatusLine().getStatusCode();
    }

    public static int startHarvest(Integer repositoryId) throws IOException {
        final HttpClient httpClient = HttpClientBuilder.create().build();
        final HttpPost httpPost = new HttpPost(String.format("%s/harvesters/%d/start",
                IntegrationTestUtil.APP_URL, repositoryId));

        httpPost.addHeader("Accept", "application/json");

        final HttpResponse response = httpClient.execute(httpPost);
        return response.getStatusLine().getStatusCode();
    }
}
