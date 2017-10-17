package nl.kb.manifest;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.Is.is;

public class ManifestFinalizerTest {


    @Test
    public void writeResourcesToManifestShouldCreateAManifestFileFromTheMetadataXML() throws IOException, SAXException, TransformerException, ParserConfigurationException {
        final InputStream in = ManifestFinalizerTest.class.getResourceAsStream("/manifest/manifest.xml");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Reader metadata = new InputStreamReader(in,"UTF-8");
        final Writer manifest = new OutputStreamWriter(out, "UTF-8");

        final ObjectResource metadataResource = getObjectResource("metadata", "check-md", "metadata.xml");
        final ObjectResource file0001 = getObjectResource("FILE_0001", "check-1", "test1.html");
        final ObjectResource file0002 = getObjectResource("FILE_0002", "check-2", "test2.pdf");
        final ObjectResource file0003 = getObjectResource("FILE_0003", "check-3", "test3.txt");
        final List<ObjectResource> objectResources = Lists.newArrayList(
                file0001, file0002, file0003
        );
        final ManifestFinalizer instance = new ManifestFinalizer();

        instance.writeResourcesToManifest(metadataResource, objectResources, metadata, manifest);

        final ManifestXmlHandler manifestXmlHandler = new ManifestXmlHandler();
        final SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()))), manifestXmlHandler);

        assertThat(manifestXmlHandler.getObjectResources(), contains(
                allOf(
                        hasProperty("localFilename", is("test1.html")),
                        hasProperty("checksum", is("check-1"))
                ),
                allOf(
                        hasProperty("localFilename", is("test2.pdf")),
                        hasProperty("checksum", is("check-2"))
                ),
                allOf(
                        hasProperty("localFilename", is("test3.txt")),
                        hasProperty("checksum", is("check-3"))
                )
        ));
    }


    private ObjectResource getObjectResource(String id, String checksum, String filename) {
        final ObjectResource objectResource = new ObjectResource();
        objectResource.setId(id);
        objectResource.setChecksum(checksum);
        objectResource.setLocalFilename(filename);
        return objectResource;
    }


}