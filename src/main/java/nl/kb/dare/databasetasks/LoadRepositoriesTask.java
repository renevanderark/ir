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
                buildRepo(dateStamp, "Utrecht", "uu:dare"),
                buildRepo(dateStamp, "Nijmegen", "ru:col_2066_13799"),
                buildRepo(dateStamp, "Groningen", "rug:publications:withFiles"),
                buildRepo(dateStamp, "Delft", "tud:A-set"),
                buildRepo(dateStamp, "Leiden", "ul:hdl_1887_4539"),
                buildRepo(dateStamp, "Maastricht", "um:publications:withFiles"),
                buildRepo(dateStamp, "Twente", "ut:publications:all"),
                buildRepo(dateStamp, "UvA", "uva:publications:withFiles"),
                buildRepo(dateStamp, "Tilburg", "uvt:publications:withFiles"),
                buildRepo(dateStamp, "VU", "vu:publications:withFiles"),
                buildRepo(dateStamp, "Wageningen", "wur:publickb"),
                buildRepo(dateStamp, "Rotterdam", "eur"),
                buildRepo(dateStamp, "Eindhoven", "tue:KB")
        ).forEach(this.repositoryDao::insert);
    }

    private Repository buildRepo(String dateStamp, String name, String set) {
        return new Repository.RepositoryBuilder()
                .setUrl(URL)
                .setName(name)
                .setMetadataPrefix(NL_DIDL_COMBINED)
                .setSet(set)
                .setDateStamp(dateStamp)
                .setEnabled(false)
                .setSchedule(HarvestSchedule.DAILY).createRepository();
    }
}
