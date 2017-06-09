package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.apache.commons.io.IOUtils;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class LoadH2SchemaTask extends Task {
    private final DBI db;

    public LoadH2SchemaTask(DBI db) {
        super("create-h2-schema");
        this.db = db;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> immutableMultimap, PrintWriter printWriter) throws Exception {
        final Handle h = db.open();

        runSQL("/database-schema/h2/repositories.sql", h);
        runSQL("/database-schema/h2/dare_preproces.sql", h);
        runSQL("/database-schema/h2/error_reports.sql", h);

        h.close();
    }

    private void runSQL(String resourceLocation, Handle h) throws IOException {
        final InputStream resource = LoadOracleSchemaTask.class.getResourceAsStream(resourceLocation);
        final String schemaSql = IOUtils.toString(resource, "UTF8");

        h.update(schemaSql);
    }
}