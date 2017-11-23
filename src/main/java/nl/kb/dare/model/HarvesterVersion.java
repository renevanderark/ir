package nl.kb.dare.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;

public class HarvesterVersion {
    private static final Logger LOG = LoggerFactory.getLogger(HarvesterVersion.class);

    @JsonProperty
    private final String name = "Objectharvester Institutionele Repositories";
    @JsonProperty
    private String version;
    {
        try {
            version = IOUtils.toString(HarvesterVersion.class.getResourceAsStream("/version.txt")
                    , Charset.defaultCharset());
        } catch (Exception e) {
            version = "version-under-development";
        }
    }
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
}
