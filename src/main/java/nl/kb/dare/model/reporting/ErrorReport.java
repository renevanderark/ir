package nl.kb.dare.model.reporting;

import nl.kb.http.HttpResponseException;
import nl.kb.dare.model.statuscodes.ErrorStatus;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ErrorReport {

    private static final String PACKAGE_FILTER = "nl.kb";
    private final Exception exception;
    private final URL url;
    private final ErrorStatus errorStatus;
    private final String dateStamp;

    public Exception getException() {
        return exception;
    }

    public ErrorReport(Exception exception, URL url, ErrorStatus errorStatus) {
        this.exception = exception;
        this.url = url;
        this.errorStatus = errorStatus;
        this.dateStamp = Instant.now().toString();
    }

    public ErrorReport(Exception exception, ErrorStatus errorStatus) {
        this(exception, null, errorStatus);
    }

    public String getFilteredStackTrace() {
        final StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : exception.getStackTrace()) {
            if (element.getClassName().startsWith(PACKAGE_FILTER)) {
                sb.append(element.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    public String getUrl() {
        return url == null ? "" : url.toString();
    }

    String getDateStamp() {
        return dateStamp;
    }

    String getErrorMessage() {
        return exception.getMessage();
    }


    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    public static List<ErrorReport> fromExceptionList(List<Exception> exceptions) {
        return exceptions.stream().map(ErrorReport::fromException).collect(toList());
    }

    public static ErrorReport fromException(Exception exception) {
        return new ErrorReport(exception,
            exception instanceof HttpResponseException
                ? ((HttpResponseException) exception).getUrl()
                : null,
            exception instanceof SAXException
                ? ErrorStatus.XML_PARSING_ERROR
                : exception instanceof IOException
                    ? ErrorStatus.IO_EXCEPTION
                    : exception instanceof  HttpResponseException
                        ? ErrorStatus.forCode(((HttpResponseException) exception).getStatusCode())
                        : ErrorStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Override
    public String toString() {
        return "ErrorReport{" +
                "exception=" + exception +
                ", url=" + url +
                ", errorStatus=" + errorStatus +
                ", dateStamp='" + dateStamp + '\'' +
                '}';
    }
}
