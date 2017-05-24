package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class LoadOracleSchemaTask extends Task {
    private final DBI db;

    public LoadOracleSchemaTask(DBI db) {
        super("create-oracle-schema");
        this.db = db;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        final Handle h = db.open();

        runSQL("/database-schema/repositories.sql", h);
        runSQL("/database-schema/dare_preproces.sql", h);
        runSQL("/database-schema/error_reports.sql", h);

        h.close();
    }

    private void runSQL(String resourceLocation, Handle h) throws IOException {
        final InputStream resource = LoadOracleSchemaTask.class.getResourceAsStream(resourceLocation);
        final String schemaSql = IOUtils.toString(resource, "UTF8");

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
