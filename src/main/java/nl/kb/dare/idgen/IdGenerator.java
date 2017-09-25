package nl.kb.dare.idgen;

import nl.kb.http.HttpResponseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

public interface IdGenerator {
    List<String> getUniqueIdentifiers(int quantity) throws SAXException, IOException, HttpResponseException;
}
