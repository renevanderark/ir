package nl.kb.dare.jobs.getrecord;

import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.repository.RepositoryDao;
import nl.kb.dare.model.statuscodes.ProcessStatus;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.http.HttpFetcher;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.manifest.ManifestFinalizer;
import nl.kb.manifest.ObjectResource;
import nl.kb.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;


public class GetRecord {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecord.class);


    private final Record record;
    private final GetRecordOperations getRecordOperations;

    GetRecord(GetRecordOperations getRecordOperations, Record record) {
        this.getRecordOperations = getRecordOperations;
        this.record = record;
    }

    public static ProcessStatus getAndRun(RepositoryDao repositoryDao, Record record,
                                          HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory,
                                          FileStorage fileStorage, XsltTransformer xsltTransformer,
                                          Consumer<ErrorReport> onError) {

        final Repository repositoryConfig = repositoryDao.findById(record.getRepositoryId());
        if (repositoryConfig == null) {
            LOG.error("SEVERE! OaiRecord missing repository configuration in database: {}", record);
            // TODO error report
            return ProcessStatus.FAILED;
        }

        final GetRecordResourceOperations resourceOperations = new GetRecordResourceOperations(
                httpFetcher, responseHandlerFactory);

        final GetRecordOperations getRecordOperations = new GetRecordOperations(
                fileStorage, httpFetcher, responseHandlerFactory, xsltTransformer,
                repositoryConfig, resourceOperations, new ManifestFinalizer(),
                onError);

        return new GetRecord(getRecordOperations, record).fetch();
    }

    ProcessStatus fetch() {

        final Optional<FileStorageHandle> fileStorageHandle = getRecordOperations.getFileStorageHandle(record);
        if (!fileStorageHandle.isPresent()) {
            return ProcessStatus.FAILED;
        }

        final FileStorageHandle handle = fileStorageHandle.get();
        final Optional<ObjectResource> metadataResource = getRecordOperations.downloadMetadata(handle, record);
        if (!metadataResource.isPresent()) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.generateManifest(handle)) {
            return ProcessStatus.FAILED;
        }

        final List<ObjectResource> objectResources = getRecordOperations.collectResources(handle);
        if (!getRecordOperations.downloadResources(handle, objectResources)) {
            return ProcessStatus.FAILED;
        }

        if (!getRecordOperations.writeFilenamesAndChecksumsToMetadata(handle, objectResources, metadataResource.get())) {
            return ProcessStatus.FAILED;
        }

        return ProcessStatus.PROCESSED;
    }
}
