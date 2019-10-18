package mc.gouv.xaf.back.data.es.model;

import java.util.List;

public class ConfigPropertiesDTO {

    private List<ConfigPropertyDTO> properties;

    public List<ConfigPropertyDTO> getProperties() {
        return properties;
    }

    public void setProperties(List<ConfigPropertyDTO> properties) {
        this.properties = properties;
    }

}
