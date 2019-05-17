package mc.gouv.af.back.data.es.model;

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
