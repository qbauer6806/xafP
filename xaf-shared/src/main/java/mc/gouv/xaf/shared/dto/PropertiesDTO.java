package mc.gouv.xaf.shared.dto;

/**
 * Modélise une donnée d'un properties
 * @author mboutelier.ext
 */
public class PropertiesDTO {

    private Integer pkProperties;

    private String demarcheId;

    private PropertiesTypeEnum type;

    private String key;

    private String value;

    public Integer getPkProperties() {
        return pkProperties;
    }

    public void setPkProperties(Integer pkProperties) {
        this.pkProperties = pkProperties;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public PropertiesTypeEnum getType() {
        return type;
    }

    public void setType(PropertiesTypeEnum type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
