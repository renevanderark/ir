package nl.kb.dare.objectharvester;

import com.google.common.collect.Lists;
import nl.kb.dare.model.reporting.ErrorReport;
import nl.kb.filestorage.FileStorageHandle;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import nl.kb.manifest.ObjectResource;
import nl.kb.stream.ByteCountOutputStream;
import nl.kb.stream.ChecksumOutputStream;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class ObjectHarvesterResourceOperations {
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    ObjectHarvesterResourceOperations(HttpFetcher httpFetcher, ResponseHandlerFactory responseHandlerFactory) {
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    List<ErrorReport> downloadResource(
            ObjectResource objectResource,
            FileStorageHandle fileStorageHandle) throws IOException, NoSuchAlgorithmException {

        final String fileLocation = objectResource.getXlinkHref();
        final String filename = createFilename(fileLocation);

        final OutputStream objectOut = fileStorageHandle.getOutputStream("resources", filename);
        final ChecksumOutputStream checksumOut = new ChecksumOutputStream("SHA-512");
        final ByteCountOutputStream byteCountOut = new ByteCountOutputStream();

        // First try to fetch the resource by encoding the url name one way (whitespace as '+')
        final String preparedUrlWithPluses = prepareUrl(fileLocation, false);
        final List<ErrorReport> firstAttemptErrors = attemptDownload(objectOut, checksumOut, byteCountOut,
                preparedUrlWithPluses);

        if (firstAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, byteCountOut, filename);
            return Lists.newArrayList();
        }

        // Then try to fetch the resource by encoding the url name another way (whitespace as '%20')
        final String preparedUrlWithPercents = prepareUrl(fileLocation, true);
        if (preparedUrlWithPercents.equals(preparedUrlWithPluses)) {
            return firstAttemptErrors;
        }

        final List<ErrorReport> secondAttemptErrors = attemptDownload(objectOut, checksumOut, byteCountOut,
                preparedUrlWithPercents);

        if (secondAttemptErrors.isEmpty()) {
            writeChecksumAndFilename(objectResource, checksumOut, byteCountOut, filename);
            return Lists.newArrayList();
        }

        return Stream
                .concat(firstAttemptErrors.stream(), secondAttemptErrors.stream())
                .collect(toList());
    }

    private void writeChecksumAndFilename(ObjectResource objectResource,
                                          ChecksumOutputStream checksumOut,
                                          ByteCountOutputStream byteCountOut,
                                          String filename)  {

        objectResource.setChecksum(checksumOut.getChecksumString());
        objectResource.setChecksumType("SHA-512");
        objectResource.setLocalFilename(filename);
        objectResource.setSize(byteCountOut.getCurrentByteCount());
    }

    private List<ErrorReport> attemptDownload(OutputStream objectOut, OutputStream checksumOut,
                                              ByteCountOutputStream byteCountOut,
                                              String preparedUrl) throws MalformedURLException {
        final HttpResponseHandler responseHandler = responseHandlerFactory
                .getStreamCopyingResponseHandler(objectOut, checksumOut, byteCountOut);

        final URL objectUrl = new URL(preparedUrl);

        httpFetcher.execute(objectUrl, responseHandler);

        return ErrorReport.fromExceptionList(responseHandler.getExceptions());
    }

    static String createFilename(String objectFile) throws MalformedURLException, UnsupportedEncodingException {
        final String decodedFilename = URLDecoder.decode(new URL(objectFile).getPath().replaceAll("/$", ""), StandardCharsets.UTF_8.name());

        return FilenameUtils.getName(decodedFilename);
    }

    private String prepareUrl(String rawUrl, boolean plusToPercent) throws UnsupportedEncodingException {
        final String name = FilenameUtils.getName(rawUrl);
        final String path = FilenameUtils.getPath(rawUrl);

        return path + encodeName(name, plusToPercent);
    }

    private String encodeName(String name, boolean plusToPercent) throws UnsupportedEncodingException {
        final String encodedName = name.equals(URLDecoder.decode(name, StandardCharsets.UTF_8.name()))
                ? URLEncoder.encode(name, StandardCharsets.UTF_8.name())
                : URLEncoder.encode(URLDecoder.decode(name, StandardCharsets.UTF_8.name()), StandardCharsets.UTF_8.name());

        return plusToPercent
                ? encodedName.replaceAll("\\+", "%20")
                : encodedName;
    }
}
