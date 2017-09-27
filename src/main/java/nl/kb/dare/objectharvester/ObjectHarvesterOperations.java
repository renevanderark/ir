package nl.kb.dare.objectharvester;

import com.google.common.collect.Lists;
import nl.kb.dare.config.FileStorageGoal;
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
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ObjectHarvesterOperations {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHarvesterOperations.class);

    private static final SAXParser saxParser;
    private static final String METADATA_XML = "metadata.xml";
    static final String MANIFEST_INITIAL_XML = "manifest.initial.xml";
    static final String MANIFEST_XML = "procesdata.xml";
    private static final String MANIFEST_XML_SHA512_CHECKSUM = "procesdata.xml.sha512.checksum";

    static {
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize sax parser");
        }
    }

    private final FileStorage processingStorage;
    private final FileStorage rejectedStorage;
    private final FileStorage doneStorage;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;
    private final XsltTransformer xsltTransformer;
    private final ObjectHarvesterResourceOperations resourceOperations;
    private final ManifestFinalizer manifestFinalizer;

    public ObjectHarvesterOperations(FileStorage processingStorage,
                                     FileStorage rejectedStorage,
                                     FileStorage doneStorage,
                                     HttpFetcher httpFetcher,
                                     ResponseHandlerFactory responseHandlerFactory,
                                     XsltTransformer xsltTransformer,
                                     ObjectHarvesterResourceOperations resourceOperations,
                                     ManifestFinalizer manifestFinalizer) {

        this.processingStorage = processingStorage;
        this.rejectedStorage = rejectedStorage;
        this.doneStorage = doneStorage;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
        this.xsltTransformer = xsltTransformer;
        this.resourceOperations = resourceOperations;
        this.manifestFinalizer = manifestFinalizer;
    }

    private FileStorage getFileStorageForGoal(FileStorageGoal goal) {
        switch (goal) {
            case DONE:
                return doneStorage;
            case REJECTED:
                return rejectedStorage;
            case PROCESSING:
            default:
                return processingStorage;
        }
    }

    Optional<FileStorageHandle> getFileStorageHandle(FileStorageGoal goal, String superSet, Record oaiRecord,
                                                     Consumer<ErrorReport> onError) {
        try {
            return Optional.of(getFileStorageForGoal(goal)
                    .create(String.format("%s/%s", superSet, oaiRecord.getIpName())));
        } catch (IOException e) {
            onError.accept(new ErrorReport(
                    new IOException("Failed to create storage location for record " + oaiRecord.getIpName(), e),
                    ErrorStatus.IO_EXCEPTION)
            );
            return Optional.empty();
        }
    }

    Optional<ObjectResource> downloadMetadata(FileStorageHandle processingStorageHandle, Record record,
                                              Repository repository, Consumer<ErrorReport> onError) {
        try {
            final String urlStr = String.format("%s?verb=GetRecord&metadataPrefix=%s&identifier=%s",
                    repository.getUrl(), repository.getMetadataPrefix(), record.getOaiIdentifier());

            final OutputStream out = processingStorageHandle.getOutputStream(METADATA_XML);
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
            objectResource.setLocalFilename(METADATA_XML);
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

    boolean generateManifest(FileStorageHandle handle, Consumer<ErrorReport> onError) {
        try {
            final InputStream metadata = handle.getFile(METADATA_XML);
            final OutputStream out = handle.getOutputStream(MANIFEST_INITIAL_XML);
            final Writer outputStreamWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8.name());

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


    List<ObjectResource> collectResources(FileStorageHandle processingStorageHandle, Consumer<ErrorReport> onError) {
        try {
            final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
            synchronized (saxParser) {
                saxParser.parse(processingStorageHandle.getFile(MANIFEST_INITIAL_XML), manifestXmlHandler);
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

    boolean downloadResources(FileStorageHandle processingStorageHandle, List<ObjectResource> objectResources,
                              Consumer<ErrorReport> onError) {
        try {
            for (ObjectResource objectResource : objectResources) {

                final List<ErrorReport> reports = resourceOperations
                        .downloadResource(objectResource, processingStorageHandle);

                if (!reports.isEmpty()) {
                    onError.accept(reports.get(0));
                    return false;
                }
            }
            return true;
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        }
    }


    boolean writeFilenamesAndChecksumsToMetadata(FileStorageHandle handle, List<ObjectResource> objectResources,
                                                 ObjectResource metadataResource, Consumer<ErrorReport> onError) {
        try {
            final InputStream in = handle.getFile(MANIFEST_INITIAL_XML);
            final OutputStream out = handle.getOutputStream(MANIFEST_XML);
            final Reader metadata = new InputStreamReader(in,StandardCharsets.UTF_8.name());
            final Writer manifest = new OutputStreamWriter(out, StandardCharsets.UTF_8.name());

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

    void moveToStorage(FileStorageGoal goal, FileStorageHandle fromStorageHandle, String prefix, Record record) {
        final FileStorage targetStorage = getFileStorageForGoal(goal);
        final FileStorageHandle targetHandle = targetStorage
                .obtainHandle(String.format("%s/%s", prefix, record.getIpName()));
        try {
            fromStorageHandle.moveTo(targetHandle);
        } catch (IOException e) {
            LOG.error("SEVERE: failed to access storage for {} record", goal, e);
        }
    }

    boolean generateManifestChecksum(FileStorageHandle handle, Consumer<ErrorReport> onError) {
        try {
            final InputStream inputStream = handle.getFile(MANIFEST_XML);
            final ChecksumOutputStream checksumOutputStream = new ChecksumOutputStream("SHA-512");
            IOUtils.copy(inputStream, checksumOutputStream);
            IOUtils.write(checksumOutputStream.getChecksumString(),
                    handle.getOutputStream(MANIFEST_XML_SHA512_CHECKSUM), Charset.defaultCharset());
            return true;
        } catch (IOException | NoSuchAlgorithmException e) {
            onError.accept(new ErrorReport(e, ErrorStatus.IO_EXCEPTION));
            return false;
        }
    }
}
