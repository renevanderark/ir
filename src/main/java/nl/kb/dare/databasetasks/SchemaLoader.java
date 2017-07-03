package nl.kb.dare.databasetasks;

import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SchemaLoader {
    private SchemaLoader() {

    }

    public static void runSQL(String resourceLocation, Handle h) throws IOException {
        final InputStream resource = SchemaLoader.class.getResourceAsStream(resourceLocation);
        final String schemaSql = IOUtils.toString(resource, StandardCharsets.UTF_8.name());

        StringBuilder sb = new StringBuilder();
        for (String line : schemaSql.split("\n")) {
            if (line.trim().length() == 0) {
                final String sql = sb.toString();
                h.update(sql);
                sb.setLength(0);
            } else {
                sb.append(line).append("\n");
            }
        }
    }
}
