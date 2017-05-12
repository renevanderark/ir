package nl.kb.xslt;

import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class PipedXsltTransformer implements XsltTransformer {
    private static final SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
    private final List<Templates> templates;


    private PipedXsltTransformer(StreamSource... styleSheets) throws IOException, TransformerConfigurationException {

        final List<TransformerConfigurationException> transformerConfigurationExceptions = new ArrayList<>();
        templates = Arrays.stream(styleSheets).map(styleSheet -> {
            try {
                return factory.newTemplates(styleSheet);
            } catch (TransformerConfigurationException e) {
                transformerConfigurationExceptions.add(e);
                return null;
            }
        }).collect(toList());

        if (!transformerConfigurationExceptions.isEmpty()) {
            throw transformerConfigurationExceptions.get(0);
        }
        if (templates.size() == 0) {
            throw new IOException("Must provide at least one xslt stream source");
        }
    }

    private List<TransformerHandler> getHandlers() throws TransformerConfigurationException {
        final List<TransformerConfigurationException> transformerConfigurationExceptions = new ArrayList<>();
        final List<TransformerHandler> transformerHandlers = templates.stream().map(template -> {
            try {
                return factory.newTransformerHandler(template);
            } catch (TransformerConfigurationException e) {
                transformerConfigurationExceptions.add(e);
                return null;
            }
        }).collect(toList());

        if (!transformerConfigurationExceptions.isEmpty()) {
            throw transformerConfigurationExceptions.get(0);
        }

        if (templates.size() > 1) {
            final TransformerHandler prevHandler = transformerHandlers.get(0);
            for (TransformerHandler transformerHandler : transformerHandlers) {
                prevHandler.setResult(new SAXResult(transformerHandler));
            }
        }
        return transformerHandlers;
    }

    public static PipedXsltTransformer newInstance(StreamSource... styleSheets) throws IOException, TransformerConfigurationException {
        return new PipedXsltTransformer(styleSheets);
    }


    @Override
    public void transform(InputStream in, Result out) throws TransformerException, UnsupportedEncodingException {
        transform(in, out, new HashMap<>());
    }

    @Override
    public void transform(InputStream in, Result out, Map<String, String> parameters) throws TransformerException, UnsupportedEncodingException {
        final List<TransformerHandler> transformerHandlers = getHandlers();
        final TransformerHandler startChain = transformerHandlers.get(0);
        final TransformerHandler endChain = transformerHandlers.get(transformerHandlers.size() - 1);
        final Transformer transformer = factory.newTransformer();

        for (Map.Entry<String, String> parameter : parameters.entrySet()) {
            for (TransformerHandler transformerHandler : transformerHandlers) {
                transformerHandler.getTransformer().setParameter(parameter.getKey(), parameter.getValue());
            }
        }

        final Reader reader = new InputStreamReader(in,"UTF-8");

        endChain.setResult(out);
        transformer.transform(new StreamSource(reader), new SAXResult(startChain));
    }
}
