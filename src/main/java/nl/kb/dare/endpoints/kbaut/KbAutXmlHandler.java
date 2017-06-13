package nl.kb.dare.endpoints.kbaut;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class KbAutXmlHandler extends DefaultHandler {

    private boolean valid = false;
    private boolean inStatus = false;

    private final StringBuilder statusBuilder = new StringBuilder();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("status")) {
            inStatus = true;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("status")) {
            inStatus = false;
            if (statusBuilder.toString().equalsIgnoreCase("ok")) {
                valid = true;
            }
            statusBuilder.setLength(0);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (inStatus) {
            statusBuilder.append(getStrippedText(ch, start, length));
        }
    }

    private String getStrippedText(char[] ch, int start, int length) {
        return new String(ch, start, length)
                .replaceAll("\0", "")
                .replaceAll("\\r\\n", "")
                .replaceAll("\\n", "");
    }


    boolean isValid() {
        return valid;
    }
}
