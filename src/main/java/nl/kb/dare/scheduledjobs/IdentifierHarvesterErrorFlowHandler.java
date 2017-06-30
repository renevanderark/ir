package nl.kb.dare.scheduledjobs;

import nl.kb.dare.model.repository.RepositoryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class IdentifierHarvesterErrorFlowHandler {
    private static final Logger LOG = LoggerFactory.getLogger(IdentifierHarvesterErrorFlowHandler.class);

    private final RepositoryController repositoryController;
    private final IdentifierHarvesterDaemon identifierHarvesterDaemon;

    IdentifierHarvesterErrorFlowHandler(
            RepositoryController repositoryController,
            IdentifierHarvesterDaemon identifierHarvesterDaemon) {

        this.repositoryController = repositoryController;
        this.identifierHarvesterDaemon = identifierHarvesterDaemon;
    }

    void handlerIdentifierHarvestException(Exception ex) {
        LOG.error("SEVERE: Harvester failed due to failing service", ex);
        repositoryController.disableAllRepositories();
        identifierHarvesterDaemon.interruptAllHarvests();
    }
}
