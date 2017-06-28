package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.dare.SchemaLoader;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

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

        SchemaLoader.runSQL("/database-schema/repositories.sql", h);
        SchemaLoader.runSQL("/database-schema/dare_preproces.sql", h);
        SchemaLoader.runSQL("/database-schema/error_reports.sql", h);

        h.close();
    }

}
