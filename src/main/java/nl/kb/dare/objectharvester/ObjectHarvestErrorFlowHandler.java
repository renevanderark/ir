package nl.kb.dare.objectharvester;

import nl.kb.dare.mail.Mailer;
import nl.kb.dare.mail.mailer.Email;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryController;
import nl.kb.dare.model.repository.RepositoryDao;

public class ObjectHarvestErrorFlowHandler {
    private final RepositoryController repositoryController;
    private final RepositoryDao repositoryDao;
    private final Mailer mailer;

    public ObjectHarvestErrorFlowHandler(RepositoryController repositoryController, RepositoryDao repositoryDao,
                                         Mailer mailer) {
        this.repositoryController = repositoryController;
        this.repositoryDao = repositoryDao;
        this.mailer = mailer;
    }


    void handleConsecutiveDownloadFailures(Integer repositoryId, int maxSequentialDownloadFailures) {
        final Repository repository = repositoryDao.findById(repositoryId);
        if (repository.getEnabled()) {
            repositoryController.disable(repositoryId);

            mailer.send(new Email()
                    .withSubject(String.format("Repository %s uitgeschakeld", repository.getName()))
                    .withBody(String.format("De repository '%s' is uitgeschakeld." + System.getProperty("line.separator") +
                                    "Er zijn %d opeenvolgende downloadfouten opgetreden.",
                            repository.getName(), maxSequentialDownloadFailures))
            );
        }
    }
}
