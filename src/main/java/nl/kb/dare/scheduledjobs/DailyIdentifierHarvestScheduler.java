package nl.kb.dare.scheduledjobs;

import com.google.common.util.concurrent.AbstractScheduledService;
import nl.kb.dare.model.RunState;
import nl.kb.dare.model.repository.HarvestSchedule;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * ScheduledRepositoryHarvester, conform spec:
 * Trigger
 * Elke dag wordt er gekeken welke harvests moeten draaien op basis van hun harvest schema,
 * wanneer er voor het laatst gedraaid is (startdatum laatste harvest) en of de harvest actief is.
 */
public class DailyIdentifierHarvestScheduler extends AbstractScheduledService {
    private static final Logger LOG = LoggerFactory.getLogger(DailyIdentifierHarvestScheduler.class);

    private final RepositoryDao repositoryDao;
    private final IdentifierHarvesterDaemon harvestRunner;

    public DailyIdentifierHarvestScheduler(RepositoryDao repositoryDao, IdentifierHarvesterDaemon harvestRunner) {
        this.repositoryDao = repositoryDao;
        this.harvestRunner = harvestRunner;
    }

    @Override
    protected void runOneIteration() throws Exception {
        try {
            repositoryDao.list().stream()
                    .filter(this::harvestShouldRun)
                    .map(Repository::getId)
                    .forEach(harvestRunner::startHarvest);

        } catch (Exception e) {
            LOG.warn("Failed to start scheduled harvests, probably caused by missing schema", e);

        }
    }

    /**
     * Slaagt wanneer een harvest gestart mag en moet worden
     * 1) Staat de repository aan (getEnabled) EN
     * 2) Is de harvest voor deze repository niet al aan het draaien (getRunState) EN
     * 3a) Is er nog niet eerder geharvest? OF
     * 3b) Is het schema dagelijks? OF
     * 3c) Is het schema wekelijks en is het vandaag >= 7 sinds laatste harvest? OF
     * 3d) Is het schema maandelijks en is het vandaag >= 1 maand sinds laatste harvest?
     *
     * @param repository de te toetsen repository
     * @return of de harvest voor deze repository mag en zou moeten draaien
     */
    private boolean harvestShouldRun(Repository repository) {
        return repository.getEnabled() &&
            harvestRunner.getHarvesterRunstate(repository.getId()) == RunState.WAITING && (
                repository.getSchedule() == HarvestSchedule.DAILY ||
                repository.getLastHarvest() == null ||
                (repository.getSchedule() == HarvestSchedule.WEEKLY &&
                        ChronoUnit.DAYS.between(repository.getLastHarvest(), LocalDate.now()) >= 7) ||
                (repository.getSchedule() == HarvestSchedule.MONTHLY &&
                        ChronoUnit.MONTHS.between(repository.getLastHarvest(), LocalDate.now()) >= 1)
            );
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.DAYS);
    }
}
