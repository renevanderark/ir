package nl.kb.dare.jobs.getrecord;

import com.google.common.collect.Lists;
import nl.kb.dare.model.preproces.Record;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.dare.model.repository.Repository;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.filestorage.FileStorage;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.manifest.ManifestFinalizer;
import nl.kb.manifest.ManifestXmlHandler;
import nl.kb.manifest.ObjectResource;
import nl.kb.stream.ByteCountOutputStream;
import nl.kb.stream.ChecksumOutputStream;
import nl.kb.xslt.XsltTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

class GetRecordOperations {
    private static final Logger LOG = LoggerFactory.getLogger(GetRecordOperations.class);

    private static final SAXParser saxParser;

    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    private final FileStorage fileStorage;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final XsltTransformer xsltTransformer;
    private final Repository repository;
    private final Consumer<ErrorReport> onError;
    private final GetRecordResourceOperations resourceOperations;
    private final ManifestFinalizer manifestFinalizer;

    GetRecordOperations(FileStorage fileStorage,
                        HttpFetcher httpFetcher,
                        ResponseHandlerFactory responseHandlerFactory,
                        XsltTransformer xsltTransformer,
                        Repository repository,
                        GetRecordResourceOperations resourceOperations,
                        ManifestFinalizer manifestFinalizer,
                        Consumer<ErrorReport> onError) {

        this.fileStorage = fileStorage;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.repository = repository;
        this.resourceOperations = resourceOperations;
        this.manifestFinalizer = manifestFinalizer;
        this.onError = onError;
    }

    Optional<FileStorageHandle> getFileStorageHandle(Record oaiRecord) {
        try {
            return Optional.of(fileStorage.create(oaiRecord.getKbObjId()));
        } catch (IOException e) {
            onError.accept(new ErrorReport(
                    new IOException("Failed to create storage location for record " + oaiRecord.getKbObjId(), e),
                    ErrorStatus.IO_EXCEPTION)
            );
            return Optional.empty();
        }
    }

    Optional<ObjectResource> downloadMetadata(FileStorageHandle fileStorageHandle, Record record) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), record.getOaiIdentifier());

            final OutputStream out = fileStorageHandle.getOutputStream("metadata.xml");
            final ChecksumOutputStream checksumOut = new ChecksumOutputStream("SHA-512");
            final ByteCountOutputStream byteCountOut = new ByteCountOutputStream();

            if (LOG.isDebugEnabled()) {
                LOG.debug("fetching record: {}", urlStr);
            }

            final HttpResponseHandler responseHandler = responseHandlerFactory
                    .getStreamCopyingResponseHandler(out, checksumOut, byteCountOut);

            httpFetcher.execute(new URL(urlStr), responseHandler);

            ErrorReport.fromExceptionList(responseHandler.getExceptions()).forEach(onError);

            final ObjectResource objectResource = new ObjectResource();
            objectResource.setLocalFilename("metadata.xml");
            objectResource.setChecksum(checksumOut.getChecksumString());
            objectResource.setId("metadata");
            objectResource.setChecksumType("SHA-512");
            objectResource.setSize(byteCountOut.getCurrentByteCount());
            return responseHandler.getExceptions().isEmpty()
                    ? Optional.of(objectResource)
                    : Optional.empty();
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return Optional.empty();
        }
    }

    boolean generateManifest(FileStorageHandle handle) {
        try {
            final InputStream metadata = handle.getFile("metadata.xml");
            final OutputStream out = handle.getOutputStream("manifest.initial.xml");
            final Writer outputStreamWriter = new OutputStreamWriter(out, "UTF8");

            xsltTransformer.transform(metadata, new StreamResult(outputStreamWriter));

            return true;
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        } catch (TransformerException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return false;
        }
    }


    List<ObjectResource> collectResources(FileStorageHandle fileStorageHandle) {
        try {
            final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
            synchronized (saxParser) {
                saxParser.parse(fileStorageHandle.getFile("manifest.initial.xml"), manifestXmlHandler);
            }
            return manifestXmlHandler.getObjectResources();
        } catch (SAXException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return Lists.newArrayList();
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return Lists.newArrayList();
        }
    }

    boolean downloadResources(FileStorageHandle fileStorageHandle, List<ObjectResource> objectResources) {
        try {
            final List<ErrorReport> errorReports = Lists.newArrayList();
            for (ObjectResource objectResource : objectResources) {

                final List<ErrorReport> reports = resourceOperations
                        .downloadResource(objectResource, fileStorageHandle);

                errorReports.addAll(reports);

            }
            errorReports.forEach(onError);
            return errorReports.isEmpty();
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        }
    }


    boolean writeFilenamesAndChecksumsToMetadata(FileStorageHandle handle, List<ObjectResource> objectResources,
                                                 ObjectResource metadataResource) {
        try {
            final InputStream in = handle.getFile("manifest.initial.xml");
            final OutputStream out = handle.getOutputStream("manifest.xml");
            final Reader metadata = new InputStreamReader(in,"UTF-8");
            final Writer manifest = new OutputStreamWriter(out, "UTF-8");

            manifestFinalizer.writeResourcesToManifest(metadataResource, objectResources, metadata, manifest);

            return true;
        } catch (IOException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        } catch (SAXException | TransformerException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.XML_PARSING_ERROR));
            return false;
        }
    }
}
