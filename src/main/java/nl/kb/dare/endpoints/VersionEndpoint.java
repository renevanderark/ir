package nl.kb.dare.endpoints;

import com.fasterxml.jackson.annotation.JsonProperty;
import nl.kb.dare.model.HarvesterVersion;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Path("/version")
public class VersionEndpoint {
    private final HarvesterVersion harvesterVersion;
    private final DBI db;

    private class ResponseData {
        @JsonProperty
        private final HarvesterVersion versionInfo;
        @JsonProperty
        private final Boolean dbUp;

        ResponseData(HarvesterVersion harvesterVersion, boolean dbUp) {
            this.versionInfo = harvesterVersion;
            this.dbUp = dbUp;
        }
    }

    public VersionEndpoint(HarvesterVersion harvesterVersion, DBI db) {
        this.harvesterVersion = harvesterVersion;
        this.db = db;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response versionInfo() {
        boolean dbUp;

        try {
            final Handle hdl = db.open();
            final List<Map<String, Object>> result = hdl.select("select 1 from dual");

            if (!result.isEmpty() && result.get(0).get("1").equals(new BigDecimal(1)) ) {
                dbUp = true;
            } else {
                dbUp = false;
            }

        } catch (Exception e) {
            dbUp = false;
        }
        return Response.ok(new ResponseData(harvesterVersion, dbUp)).build();
    }
}
