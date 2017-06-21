package nl.kb.dare.tasks;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import io.dropwizard.servlets.tasks.Task;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;

import java.io.PrintWriter;

public class LoadRepositoriesTask extends Task {
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
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Utrecht", "nl_didl_combined", "uu:dare", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Nijmegen", "nl_didl_combined", "ru:col_2066_13799", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Groningen", "nl_didl_combined", "rug:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Delft", "nl_didl_combined", "tud:A-set", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Leiden", "nl_didl_combined", "ul:hdl_1887_4539", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Maastricht", "nl_didl_combined", "um:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Twente", "nl_didl_combined", "ut:66756C6C746578743D7075626C6963", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "UvA", "nl_didl_combined", "uva:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),            new Repository("http://oai.gharvester.dans.knaw.nl/", "Tilburg", "nl_didl_combined", "uvt:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "VU", "nl_didl_combined", "vu:publications:withFiles", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Wageningen", "nl_didl_combined", "wur:publickb", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Rotterdam", "nl_didl_combined", "eur", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Eindhoven", "nl_didl_combined", "tue:KB", dateStamp, false, HarvestSchedule.DAILY)
        ).forEach(this.repositoryDao::insert);
    }
}
