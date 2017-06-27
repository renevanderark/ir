package nl.kb.manifest;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static nl.kb.manifest.ManifestFinalizer.METS_NS;
import static nl.kb.manifest.ManifestFinalizer.XLINK_NS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

public class ManifestFinalizerTest {
    private static final DocumentBuilder docBuilder;
    static {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            docBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize sax parser", e);
        }
    }

    @Test
    public void writeResourcesToManifestShouldCreateAManifestFileFromTheMetadataXML() throws IOException, SAXException, TransformerException {
        final InputStream in = ManifestFinalizerTest.class.getResourceAsStream("/manifest/manifest.xml");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Reader metadata = new InputStreamReader(in,"UTF-8");
        final Writer manifest = new OutputStreamWriter(out, "UTF-8");

        final ObjectResource metadataResource = getObjectResource("metadata", "check-md", "type-md", "metadata.xml");
        final ObjectResource file0001 = getObjectResource("FILE_0001", "check-1", "type-1", "test 1.html");
        final ObjectResource file0002 = getObjectResource("FILE_0002", "check-2", "type-2", "test 2.pdf");
        final ObjectResource file0003 = getObjectResource("FILE_0003", "check-3", "type-3", "test 3.txt");
        final List<ObjectResource> objectResources = Lists.newArrayList(
                file0001, file0002, file0003
        );
        final ManifestFinalizer instance = new ManifestFinalizer();

        instance.writeResourcesToManifest(metadataResource, objectResources, metadata, manifest);


        final Document resultDoc = docBuilder.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), StandardCharsets.UTF_8.name())));
        final NodeList fileNodes = resultDoc.getElementsByTagNameNS(METS_NS, "file");
        final List<String> checksums = Lists.newArrayList();
        final List<String> checksumTypes = Lists.newArrayList();
        final List<String> fileUrls = Lists.newArrayList();
        for (int i = 0; i < fileNodes.getLength(); i++) {
            final Node fileNode = fileNodes.item(i);
            fileUrls.add(getFLocatNode(fileNode).getAttributes().getNamedItemNS(XLINK_NS, "href").getNodeValue());
            checksums.add(fileNode.getAttributes().getNamedItem("CHECKSUM").getNodeValue());
            checksumTypes.add(fileNode.getAttributes().getNamedItem("CHECKSUMTYPE").getNodeValue());
        }

        assertThat(checksums, contains("check-md", "check-1", "check-2", "check-3"));
        assertThat(checksumTypes, contains("type-md", "type-1", "type-2", "type-3"));
        assertThat(fileUrls, contains(
                "file://./metadata.xml",
                "file://./resources/test%201.html",
                "file://./resources/test%202.pdf",
                "file://./resources/test%203.txt"
        ));
    }


    private ObjectResource getObjectResource(String id, String checksum, String checksumType, String filename) {
        final ObjectResource objectResource = new ObjectResource();
        objectResource.setId(id);
        objectResource.setChecksum(checksum);
        objectResource.setChecksumType(checksumType);
        objectResource.setLocalFilename(filename);
        return objectResource;
    }


    private Node getFLocatNode(Node fileNode) {
        final NodeList childNodes = fileNode.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node item = childNodes.item(i);
            if (item.getLocalName() != null && item.getLocalName().equalsIgnoreCase("flocat")) {
                return item;
            }
        }
        return null;
    }

}