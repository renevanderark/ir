package nl.kb.manifest;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

public class ManifestFinalizer {

    private static final DocumentBuilder docBuilder;
    private static final TransformerFactory transformerFactory;

    static {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Failed to initialize sax parser");
        }
    }

    public static final String METS_NS = "http://www.loc.gov/METS/";
    public static final String XLINK_NS = "http://www.w3.org/1999/xlink";

    public void writeResourcesToManifest(ObjectResource metadataResource, List<ObjectResource> objectResources, Reader metadata, Writer manifest)
            throws IOException, SAXException, TransformerException {

        synchronized (docBuilder) {
            final Document document = docBuilder.parse(new InputSource(metadata));
            final NodeList fileNodes = document.getElementsByTagNameNS(METS_NS, "file");
            final Transformer transformer = transformerFactory.newTransformer();

            for (int i = 0; i < fileNodes.getLength(); i++) {
                final Node fileNode = fileNodes.item(i);

                final Optional<String> fileId = getAttribute(fileNode, "ID");
                if (!fileId.isPresent()) {
                    throw new IOException("ID attribute not set for file node in metadata.xml");
                }
                if (fileId.get().equals("metadata")) {
                    setAttribute(document, fileNode, "CHECKSUM", metadataResource.getChecksum());
                    setAttribute(document, fileNode, "SIZE", Long.toString(metadataResource.getSize()));

                } else {
                    writeResourceFile(objectResources, document, fileNode, fileId.get());
                }
            }
            transformer.transform(new DOMSource(document), new StreamResult(manifest));
        }
    }

    private void writeResourceFile(List<ObjectResource> objectResources, Document document, Node fileNode, String fileId) throws IOException {
        final Optional<ObjectResource> currentResource = findObjectResourceForFileId(objectResources, fileId);
        if (!currentResource.isPresent()) {
            throw new IOException("Expected file resource is not present for metadata.xml: " + fileId);
        }

        setAttribute(document, fileNode, "CHECKSUM", currentResource.get().getChecksum());
        setAttribute(document, fileNode, "SIZE", Long.toString(currentResource.get().getSize()));
        setAttribute(document, fileNode, "contentDispositionHeaderValue",
                currentResource.get().getContentDisposition());
        setAttribute(document, fileNode, "contentTypeHeaderValue",
                currentResource.get().getContentType());
        setXlinkHref(fileNode, currentResource.get());
    }

    private void setXlinkHref(Node fileNode, ObjectResource currentResource) throws IOException {
        final Optional<Node> fLocatNode = getFirstChildByLocalName(fileNode, "FLocat");
        if (!fLocatNode.isPresent()) {
            throw new IOException("File node does not have an FLocat child in metadata.xml");
        }

        final Node xlinkHrefAttribute = fLocatNode.get().getAttributes().getNamedItemNS(XLINK_NS, "href");
        if (xlinkHrefAttribute == null) {
            throw new IOException("FLocat node does not have xlink:href attribute in metadata.xml");
        }

        xlinkHrefAttribute.setNodeValue(
                "file://./resources/" +
                        URLEncoder.encode(currentResource.getLocalFilename(), StandardCharsets.UTF_8.name())
                                .replaceAll("\\+", "%20")
        );
    }

    private Optional<ObjectResource> findObjectResourceForFileId(List<ObjectResource> objectResources, String fileId) {
        return objectResources
                .stream().filter(obj -> obj.getId() != null && obj.getId().equals(fileId))
                .findAny();
    }

    private Optional<String> getAttribute(Node node, String name) {
        final Node namedItem = node.getAttributes().getNamedItem(name);
        if (namedItem == null) { return Optional.empty(); }

        final String nodeValue = namedItem.getNodeValue();
        return nodeValue == null ? Optional.empty() : Optional.of(nodeValue);
    }

    private void setAttribute(Document document, Node node, String name, String value) {
        final NamedNodeMap attributes = node.getAttributes();
        final Node newAttribute = document.createAttribute(name);
        newAttribute.setNodeValue(value);
        attributes.setNamedItem(newAttribute);
    }

    private Optional<Node> getFirstChildByLocalName(Node parent, String localName) {
        final NodeList childNodes = parent.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node item = childNodes.item(i);
            if (item.getLocalName() != null && item.getLocalName().equalsIgnoreCase(localName)) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }
}
