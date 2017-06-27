package nl.kb.dare.nbn;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

class NumbersXmlHandler extends DefaultHandler {
    private final List<Long> result = new ArrayList<>();

    private boolean inNBNnode = false;

    private final StringBuilder currentNBN = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("nbn")) {
            inNBNnode = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("nbn")) {
            inNBNnode = false;
            result.add(Long.parseLong(currentNBN.toString()));
            currentNBN.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inNBNnode) {
            currentNBN.append(new String(ch, start, length));
        }
    }

    List<Long> getResult() {
        return result;
    }
}
