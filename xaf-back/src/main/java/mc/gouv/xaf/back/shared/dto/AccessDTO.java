package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Modélise un accès
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessDTO {

    private Integer pkAccess;

    private String demarcheId;

    private Integer usagerId;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;

    private JsonNode contenu;
    
    @JsonIgnore
    private boolean updated = false;

    public Integer getPkAccess() {
        return pkAccess;
    }

    public void setPkAccess(Integer pkAccess) {
        this.pkAccess = pkAccess;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public JsonNode getContenu() {
        return contenu;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }
    
    public Date getDateCreation() {
        return dateCreation;
    }

    public void setContenu(JsonNode contenu) {
        this.contenu = contenu;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
    
}
