package nl.kb.manifest;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class ManifestXmlHandler extends DefaultHandler {


    private ObjectResource currentResource = new ObjectResource();
    private final List<ObjectResource> objectResources = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("mets:file")) {
            currentResource = new ObjectResource();
            currentResource.setId(attributes.getValue("ID"));
            currentResource.setChecksumType(attributes.getValue("CHECKSUMTYPE"));
            currentResource.setChecksum(attributes.getValue("CHECKSUM"));
            final String size = attributes.getValue("SIZE");
            if (size != null) {
                currentResource.setSize(Long.parseLong(size));
            }
        } else if (qName.equals("mets:FLocat")) {
            currentResource.setXlinkHref(attributes.getValue("xlink:href"));
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("mets:file")) {
            objectResources.add(currentResource);
        }
    }

    public List<ObjectResource> getObjectResources() {
        return objectResources.stream()
                .filter(objectResource -> !objectResource.getId().equals("metadata"))
                .collect(toList());
    }

    public List<ObjectResource> getObjectResourcesIncludingMetadata() {
        return objectResources;
    }
}
