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
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Utrecht", "nl_didl_norm", "uu", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Nijmegen", "nl_didl_norm", "ru", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Groningen", "nl_didl_norm", "rug", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Delft", "nl_didl_norm", "tud", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Leiden", "nl_didl_norm", "ul", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Maastricht", "nl_didl_norm", "um", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Twente", "nl_didl_norm", "ut", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "UvA", "nl_didl_norm", "uva", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Tilburg", "nl_didl_norm", "uvt", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "VU", "nl_didl_norm", "vu", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Wageningen", "nl_didl_norm", "wur", dateStamp, false, HarvestSchedule.DAILY),
            new Repository("http://oai.gharvester.dans.knaw.nl/", "Rotterdam", "nl_didl_norm", "eur", dateStamp, false, HarvestSchedule.DAILY)
        ).forEach(this.repositoryDao::insert);
    }
}
