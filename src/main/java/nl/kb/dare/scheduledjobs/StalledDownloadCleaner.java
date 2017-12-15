package nl.kb.dare.scheduledjobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.http.Monitable;

import java.util.concurrent.TimeUnit;

public class StalledDownloadCleaner extends AbstractScheduledService {
    private final Monitable httpFetcherForObjectHarvest;
    private final Integer maximumDownloadStallTimeMs;

    public StalledDownloadCleaner(Monitable httpFetcherForObjectHarvest, Integer maximumDownloadStallTimeMs) {
        this.httpFetcherForObjectHarvest = httpFetcherForObjectHarvest;
        this.maximumDownloadStallTimeMs = maximumDownloadStallTimeMs;
    }

    @Override
    protected void runOneIteration() throws Exception {
        httpFetcherForObjectHarvest.disconnectStalledConnections(maximumDownloadStallTimeMs);
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.SECONDS);
    }
}
