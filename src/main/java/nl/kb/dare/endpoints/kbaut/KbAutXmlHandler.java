package nl.kb.dare.endpoints.kbaut;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class KbAutXmlHandler extends DefaultHandler {

    private boolean valid = false;
    private String username = "";

    private final StringBuilder statusBuilder = new StringBuilder();
    private final StringBuilder usernameBuilder = new StringBuilder();

    private boolean inStatus = false;
    private boolean inUsername = false;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("status")) {
            inStatus = true;
        } else if (qName.equals("name")) {
            inUsername = true;

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
        } else if (qName.equals("name")) {
            inUsername = false;
            username = usernameBuilder.toString();
            usernameBuilder.setLength(0);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inStatus) {
            statusBuilder.append(getStrippedText(ch, start, length));
        } else if (inUsername) {
            usernameBuilder.append(getStrippedText(ch, start, length));
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

    public String getUsername() {
        return username;
    }
}
