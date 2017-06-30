package nl.kb.dare.scheduledjobs;

import nl.kb.dare.mail.mailer.Email;
import nl.kb.dare.mail.Mailer;
import nl.kb.dare.model.repository.RepositoryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

class IdentifierHarvesterErrorFlowHandler {
    private static final Logger LOG = LoggerFactory.getLogger(IdentifierHarvesterErrorFlowHandler.class);

    private final RepositoryController repositoryController;
    private final IdentifierHarvesterDaemon identifierHarvesterDaemon;
    private final Mailer mailer;

    IdentifierHarvesterErrorFlowHandler(
            RepositoryController repositoryController,
            IdentifierHarvesterDaemon identifierHarvesterDaemon,
            Mailer mailer
    ) {

        this.repositoryController = repositoryController;
        this.identifierHarvesterDaemon = identifierHarvesterDaemon;
        this.mailer = mailer;
    }

    void handlerIdentifierHarvestException(Exception ex) {
        LOG.error("SEVERE: Harvester failed due to failing service", ex);
        repositoryController.disableAllRepositories();
        identifierHarvesterDaemon.interruptAllHarvests();

        final Email email = new Email()
                .withSubject("Harvest van identifiers gefaald")
                .withBody(getMailBody(ex));

        mailer.send(email);
    }

    private String getMailBody(Exception ex) {
        final String traceMsg = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString).collect(joining("\n"));

        return String.format("%s:%n%n%s", ex.getMessage(), traceMsg);
    }
}
