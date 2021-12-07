package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

/**
 * 
 * Modélise un template
 * 
 * @author qdeme
 *
 */
public class TemplateDTO {

    private Integer pkTemplates;

    private String demarcheId;

    private String code;

    private String contenu;

    private String langue;

    private Date dateModif;
    
    @JsonIgnore
    private boolean updated = false;

    public Integer getPkTemplates() {
        return pkTemplates;
    }

    public void setPkTemplates(Integer pkTemplates) {
        this.pkTemplates = pkTemplates;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public Date getDateModif() {
        return dateModif;
    }

    public void setDateModif(Date dateModif) {
        this.dateModif = dateModif;
    }
}
