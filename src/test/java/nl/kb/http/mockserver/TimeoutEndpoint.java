package nl.kb.http.mockserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

@Path("/timeout")
public class TimeoutEndpoint {

    @GET
    @Path("/no-data")
    public Response timeoutWithoutData() {
        final StreamingOutput downloadOutput = out -> {
            final long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 1000L) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {

                }
            }
        };
        return Response.ok(downloadOutput).build();
    }
}
