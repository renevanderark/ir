package nl.kb.xslt;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PipedXsltTransformerTest {

    private InputStream sheet1;
    private InputStream xmlIn;
    private InputStream sheet2;

    @Before
    public void setUp() {
        sheet1 = PipedXsltTransformerTest.class.getResourceAsStream("/xslt/sheet1.xsl");
        sheet2 = PipedXsltTransformerTest.class.getResourceAsStream("/xslt/sheet2.xsl");

        xmlIn = PipedXsltTransformerTest.class.getResourceAsStream("/xslt/input.xml");
    }

    @Test(expected = IOException.class)
    public void newInstanceShouldThrowWhenNoStylesheetsAreProvided() throws IOException, TransformerException {
        PipedXsltTransformer.newInstance();
    }


    @Test(expected = TransformerConfigurationException.class)
    public void newInstanceShouldThrowWhenInvalidStylesheetWasProvided() throws IOException, TransformerConfigurationException {
        PipedXsltTransformer.newInstance(
                new StreamSource(IOUtils.toInputStream("<xsl", StandardCharsets.UTF_8.name()))
        );
    }

    @Test
    public void transformShouldApplyStylesheetsWithParametersInOrder() throws IOException, TransformerException {
        final PipedXsltTransformer instance = PipedXsltTransformer.newInstance(
                new StreamSource(sheet1), new StreamSource(sheet2)
        );

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StreamResult streamResult = new StreamResult(out);
        final Map<String, String> parameters = new HashMap<>();

        parameters.put("param1", "paramValue1");
        parameters.put("param2", "paramValue2");

        instance.transform(xmlIn, streamResult, parameters);

        assertThat(new String(out.toByteArray()), is(
                "<output><kept>Kept</kept><param1>paramValue1</param1><param2>paramValue2</param2></output>"
        ));
    }

    @Test
    public void transformShouldApplyStylesheetsWithoutParameters() throws IOException, TransformerException {
        final PipedXsltTransformer instance = PipedXsltTransformer.newInstance(new StreamSource(sheet1));

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final StreamResult streamResult = new StreamResult(out);
        final Map<String, String> parameters = new HashMap<>();

        instance.transform(xmlIn, streamResult);

        assertThat(new String(out.toByteArray()), is("<output><kept>Kept</kept></output>"));
    }
}