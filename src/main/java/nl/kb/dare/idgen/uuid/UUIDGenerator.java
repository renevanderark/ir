package nl.kb.dare.idgen.uuid;

import nl.kb.dare.idgen.IdGenerator;
import nl.kb.http.HttpResponseException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UUIDGenerator implements IdGenerator {
    @Override
    public List<String> getUniqueIdentifiers(int quantity) throws SAXException, IOException, HttpResponseException {
        final List<String> result = new ArrayList<>();

        for (int i = 0; i < quantity; i++) {
            result.add(UUID.randomUUID().toString());
        }

        return result;
    }
}
