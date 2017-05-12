package nl.kb.oaipmh;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Optional;
import java.util.function.Consumer;

public class ListIdentifiersXmlHandler extends DefaultHandler {

    private static final String RESUMPTION_TOKEN_ELEMENT = "resumptionToken";
    private static final String DATE_STAMP_ELEMENT = "datestamp";
    private static final String HEADER_ELEMENT = "header";
    private static final String IDENTIFIER_ELEMENT = "identifier";

    private final Consumer<OaiRecordHeader> onOaiRecord;

    private OaiRecordHeader currentOaiRecordHeader = new OaiRecordHeader();

    private boolean inResumptionToken = false;
    private boolean inDateStamp = false;
    private boolean inIdentifier = false;

    private String resumptionToken = null;
    private String lastDateStamp = null;

    private StringBuilder resumptionTokenBuilder = new StringBuilder();
    private StringBuilder dateStampBuilder = new StringBuilder();
    private StringBuilder identifierBuilder = new StringBuilder();

    private ListIdentifiersXmlHandler(Consumer<OaiRecordHeader> onOaiRecordHeader) {
        this.onOaiRecord = onOaiRecordHeader;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) {
        switch (qName) {
            case RESUMPTION_TOKEN_ELEMENT: startResumptionToken(); break;
            case DATE_STAMP_ELEMENT: startDateStamp(); break;
            case HEADER_ELEMENT: startOaiRecord(attributes); break;
            case IDENTIFIER_ELEMENT: startIdentifier(); break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) {
        if (inResumptionToken) {
            handleResumptionToken(ch, start, length);
        } else if (inDateStamp) {
            handleDateStamp(ch, start, length);
        } else if (inIdentifier) {
            handleIdentifier(ch, start, length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        switch (qName) {
            case RESUMPTION_TOKEN_ELEMENT: endResumptionToken(); break;
            case DATE_STAMP_ELEMENT: endDateStamp(); break;
            case HEADER_ELEMENT: endOaiRecord(); break;
            case IDENTIFIER_ELEMENT: endIdentifier(); break;

        }
    }

    private void handleIdentifier(char[] ch, int start, int length) {
        identifierBuilder.append(getStrippedText(ch, start, length));
    }


    private void handleDateStamp(char[] ch, int start, int length) {
        dateStampBuilder.append(getStrippedText(ch, start, length));
    }

    private void handleResumptionToken(char[] ch, int start, int length) {
        resumptionTokenBuilder.append(getStrippedText(ch, start, length));
    }


    private String getStrippedText(char[] ch, int start, int length) {
        return new String(ch, start, length)
                .replaceAll("\0", "")
                .replaceAll("\\r\\n", "")
                .replaceAll("\\n", "");
    }

    private void startDateStamp() {
        inDateStamp = true;
        dateStampBuilder.setLength(0);
    }

    private void startResumptionToken() {
        inResumptionToken = true;
        resumptionTokenBuilder.setLength(0);
    }

    private void startIdentifier() {
        inIdentifier = true;
        identifierBuilder.setLength(0);
    }

    private void endOaiRecord() {
        onOaiRecord.accept(currentOaiRecordHeader);
    }

    private void endDateStamp() {

        inDateStamp = false;
        lastDateStamp = dateStampBuilder.toString();
        currentOaiRecordHeader.setDateStamp(dateStampBuilder.toString());
    }

    private void endResumptionToken() {
        inResumptionToken = false;
        resumptionToken = resumptionTokenBuilder.toString();
    }

    private void endIdentifier() {
        inIdentifier = false;
        currentOaiRecordHeader.setIdentifier(identifierBuilder.toString());
    }

    private void startOaiRecord(Attributes attributes) {
        currentOaiRecordHeader = new OaiRecordHeader();
        final String statusAttr = attributes.getValue("status");
        if (statusAttr != null && statusAttr.equalsIgnoreCase("deleted")) {
            currentOaiRecordHeader.setOaiStatus(OaiStatus.DELETED);
        } else {
            currentOaiRecordHeader.setOaiStatus(OaiStatus.AVAILABLE);
        }
    }

    public Optional<String> getResumptionToken() {
        return resumptionToken == null || resumptionToken.trim().length() == 0
                ? Optional.empty()
                : Optional.of(resumptionToken);
    }

    Optional<String> getLastDateStamp() {
        return lastDateStamp == null || lastDateStamp.trim().length() == 0
                ? Optional.empty()
                : Optional.of(lastDateStamp);
    }

    public static ListIdentifiersXmlHandler getNewInstance(Consumer<OaiRecordHeader> onOaiRecordHeader) {

        return new ListIdentifiersXmlHandler(onOaiRecordHeader);
    }
}
