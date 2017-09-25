package nl.kb.dare.idgen.nbn;

import nl.kb.dare.idgen.IdGenerator;
import nl.kb.http.HttpFetcher;
import nl.kb.http.HttpResponseException;
import nl.kb.http.HttpResponseHandler;
import nl.kb.http.responsehandlers.ResponseHandlerFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class NumbersController implements IdGenerator {
    private final String numbersEndpoint;
    private final HttpFetcher httpFetcher;
    private final ResponseHandlerFactory responseHandlerFactory;

    public NumbersController(String numbersEndpoint, HttpFetcher httpFetcher,
                             ResponseHandlerFactory responseHandlerFactory) {

        this.numbersEndpoint = numbersEndpoint;
        this.httpFetcher = httpFetcher;
        this.responseHandlerFactory = responseHandlerFactory;
    }

    @Override
    public List<Long> getNumbers(int quantity) throws SAXException, IOException, HttpResponseException {
        final NumbersXmlHandler numbersHandler = new NumbersXmlHandler();
        final HttpResponseHandler responseHandler = responseHandlerFactory.getSaxParsingHandler(numbersHandler);
        httpFetcher.execute(new URL(String.format("%s?qt=%d", numbersEndpoint, quantity)), responseHandler);
        responseHandler.throwAnyException();
        return numbersHandler.getResult();
    }
}
