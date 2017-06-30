package nl.kb.dare.model.reporting;

import nl.kb.dare.model.statuscodes.ErrorStatus;
import nl.kb.http.HttpResponseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ErrorReport {

    private static final String PACKAGE_FILTER = "nl.kb";
    private final Exception exception;
    private final URL url;
    private final ErrorStatus errorStatus;

    public Exception getException() {
        return exception;
    }

    public ErrorReport(Exception exception, URL url, ErrorStatus errorStatus) {
        this.exception = exception;
        this.url = url;
        this.errorStatus = errorStatus;
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


    public String getErrorMessage() {
        return exception.getMessage();
    }

    public Integer getStatusCode() { return errorStatus.getCode(); }


    public ErrorStatus getErrorStatus() {
        return errorStatus;
    }

    public static List<ErrorReport> fromExceptionList(List<Exception> exceptions) {
        return exceptions.stream().map(ErrorReport::fromException).collect(toList());
    }

    private static ErrorReport fromException(Exception exception) {
        return new ErrorReport(exception, getUrl(exception), getErrorStatus(exception));
    }

    private static URL getUrl(Exception exception) {
        return exception instanceof HttpResponseException
            ? ((HttpResponseException) exception).getUrl()
            : null;
    }

    private static ErrorStatus getErrorStatus(Exception exception) {
        return exception instanceof SAXException
            ? ErrorStatus.XML_PARSING_ERROR
            : getErrorStatusForIOException(exception);
    }

    private static ErrorStatus getErrorStatusForIOException(Exception exception) {
        return exception instanceof IOException
            ? ErrorStatus.IO_EXCEPTION
            : getErrorStatusForCode(exception);
    }

    private static ErrorStatus getErrorStatusForCode(Exception exception) {
        return exception instanceof HttpResponseException
            ? ErrorStatus.forCode(((HttpResponseException) exception).getStatusCode())
            : ErrorStatus.INTERNAL_SERVER_ERROR;
    }
}
