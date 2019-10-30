package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 
 * Modélise une donnée d'une demande
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeDataDTO {

    private Integer pkDemandesData;

    private Integer demandeId;
    
    private String key;

    private String value;
    
    @JsonIgnore
    boolean updated = false;

    public Integer getPkDemandesData() {
        return pkDemandesData;
    }

    public void setPkDemandesData(Integer pkDemandesData) {
        this.pkDemandesData = pkDemandesData;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
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

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
}
