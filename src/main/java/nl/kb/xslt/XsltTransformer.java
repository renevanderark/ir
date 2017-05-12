package nl.kb.xslt;

import javax.xml.transform.Result;
import javax.xml.transform.TransformerException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface XsltTransformer {

    void transform(InputStream in, Result out) throws TransformerException, UnsupportedEncodingException;

    void transform(InputStream in, Result out, Map<String, String> parameters) throws TransformerException, UnsupportedEncodingException;
}
