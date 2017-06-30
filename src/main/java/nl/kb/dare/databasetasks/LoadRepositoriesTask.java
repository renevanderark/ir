package nl.kb.dare.databasetasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import java.io.PrintWriter;

public class LoadRepositoriesTask extends Task {
    private static final String URL = "http://oai.gharvester.dans.knaw.nl/";
    private static final String NL_DIDL_COMBINED = "nl_didl_combined";
    private final RepositoryDao repositoryDao;

    public LoadRepositoriesTask(RepositoryDao repositoryDao) {
        super("load-repositories");
        this.repositoryDao = repositoryDao;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> params, PrintWriter printWriter) throws Exception {
        final String dateStamp = params.containsKey("datestamp")
                ? params.get("datestamp").iterator().next() : null;
        Lists.newArrayList(
            new Repository(URL, "Utrecht", NL_DIDL_COMBINED, "uu:dare", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Nijmegen", NL_DIDL_COMBINED, "ru:col_2066_13799", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Groningen", NL_DIDL_COMBINED, "rug:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Delft", NL_DIDL_COMBINED, "tud:A-set", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Leiden", NL_DIDL_COMBINED, "ul:hdl_1887_4539", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Maastricht", NL_DIDL_COMBINED, "um:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Twente", NL_DIDL_COMBINED, "ut:66756C6C746578743D7075626C6963", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "UvA", NL_DIDL_COMBINED, "uva:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),            new Repository(URL, "Tilburg", NL_DIDL_COMBINED, "uvt:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "VU", NL_DIDL_COMBINED, "vu:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Wageningen", NL_DIDL_COMBINED, "wur:publickb", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Rotterdam", NL_DIDL_COMBINED, "eur", dateStamp, false, HarvestSchedule.DAILY),
            new Repository(URL, "Eindhoven", NL_DIDL_COMBINED, "tue:KB", dateStamp, false, HarvestSchedule.DAILY)
        ).forEach(this.repositoryDao::insert);
    }
}
