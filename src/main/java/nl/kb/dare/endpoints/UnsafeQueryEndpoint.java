package nl.kb.dare.endpoints;

import oracle.sql.CLOB;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/unsafe")
public class UnsafeQueryEndpoint {
    private final DBI db;

    public UnsafeQueryEndpoint(DBI db) {
        this.db = db;
    }

    @GET
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response query(@QueryParam("q") String query) throws SQLException, IOException {
        final Handle h = db.open();
        final List<Map<String, Object>> result = h.select(query);
        final List<Map<String, String>> serResult = new ArrayList<>();

        for (Map<String, Object> stringObjectMap : result) {
            final Map<String, String> row = new HashMap<>();
            for (String s : stringObjectMap.keySet()) {
                final Object value = stringObjectMap.get(s);
                if (value != null && value instanceof CLOB) {
                    row.put(s, IOUtils.toString(((CLOB) value).binaryStreamValue(), "UTF-8"));
                } else {
                    row.put(s, value == null ? null : value.toString());
                }
            }
            serResult.add(row);
        }

        h.close();
        return Response.ok(serResult).build();
    }

}
