package nl.kb.http;

import java.util.List;
import java.util.Map;

public interface HeaderConsumer {
    void consumeHeaders(Map<String, List<String>> headerFields);
}
