package nl.kb.dare.objectharvester;

import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.manifest.ObjectResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class ObjectHarvester {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvester.class);


    private final ObjectHarvesterOperations getRecordOperations;

    ObjectHarvester(ObjectHarvesterOperations getRecordOperations) {
        this.getRecordOperations = getRecordOperations;
    }

    public static ProcessStatus getAndRun(RepositoryDao repositoryDao, Record record,
                                          ObjectHarvesterOperations getRecordOperations,
                                          Consumer<ErrorReport> onError) {

        final Repository repositoryConfig = repositoryDao.findById(record.getRepositoryId());
        if (repositoryConfig == null) {
            LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", record);
            return ProcessStatus.FAILED;
        }

        return new ObjectHarvester(getRecordOperations)
                .fetch(record, repositoryConfig, onError);
    }

    ProcessStatus fetch(Record record, Repository repositoryConfig, Consumer<ErrorReport> onError) {

        final Optional<FileStorageHandle> fileStorageHandle = getRecordOperations.getFileStorageHandle(record, onError);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = fileStorageHandle.get();
        final Optional<ObjectResource> metadataResource = getRecordOperations.downloadMetadata(handle, record,
                repositoryConfig, onError);
        if (!metadataResource.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.generateManifest(handle, onError)) {
            return ProcessStatus.FAILED;
        }

        final List<ObjectResource> objectResources = getRecordOperations.collectResources(handle, onError);
        if (!getRecordOperations.downloadResources(handle, objectResources, onError)) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations
                .writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get(), onError)) {
            return ProcessStatus.FAILED;
        }

        return ProcessStatus.PROCESSED;
    }
}
