package nl.kb.dare.objectharvester;

import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.manifest.ObjectResource;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class ObjectHarvester {
    private final ObjectHarvesterOperations getRecordOperations;

    public ObjectHarvester(ObjectHarvesterOperations getRecordOperations) {
        this.getRecordOperations = getRecordOperations;
    }

    public ProcessStatus harvestPublication(Record record, Repository repositoryConfig, Consumer<ErrorReport> onError) {

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
