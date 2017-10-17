package nl.kb.manifest;

import com.google.common.collect.Sets;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

public class ManifestXmlHandler extends DefaultHandler {
    private static final Set<String> readNodes = Sets.newHashSet(
            "downloadURL", "sha512", "fileSize"
    );
    private final List<ObjectResource> objectResources = new ArrayList<>();

    private final StringBuilder nodeValueBuilder = new StringBuilder();

    private ObjectResource currentResource = new ObjectResource();
    private boolean inReadNode = false;


    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("file")) {
            currentResource = new ObjectResource();
            currentResource.setId(attributes.getValue("ID"));
            currentResource.setLocalFilename(attributes.getValue("name"));
        } else if (readNodes.contains(qName)) {
            inReadNode = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (inReadNode) {
            nodeValueBuilder.append(getStrippedText(ch, start, length));
        }
    }

    private String getStrippedText(char[] ch, int start, int length) {
        return new String(ch, start, length)
                .replaceAll("\0", "")
                .replaceAll("\\r\\n", "")
                .replaceAll("\\n", "");
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("file")) {
            objectResources.add(currentResource);
        } else if(qName.equals("downloadURL")) {
            currentResource.setDownloadUrl(nodeValueBuilder.toString());
        } else if(qName.equals("sha512")) {
            currentResource.setChecksum(nodeValueBuilder.toString());
        } else if(qName.equals("fileSize") && nodeValueBuilder.toString().length() > 0) {
            currentResource.setSize(Long.parseLong(nodeValueBuilder.toString()));
        }
        nodeValueBuilder.setLength(0);
        inReadNode = false;
    }

    public List<ObjectResource> getObjectResources() {
        return objectResources.stream()
                .filter(objectResource -> !objectResource.getId().equals("metadata"))
                .collect(toList());
    }

}
