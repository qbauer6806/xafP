package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Modélise un courrier généré pour une demande
 * 
 * @author qdeme
 *
 */
public class DemandeCourrierDTO {

    private Integer pkCourrier;
    
    private Integer demandeId;
    
    private DemandeStatutDTO fkStatut;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    
    private String name;
    
    private String url;
    
    private String meta;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date datePrinted;
    
    private String identifiant;
    
    private String demandeIdentifiant;

    public Integer getPkCourrier() {
        return pkCourrier;
    }

    public void setPkCourrier(Integer pkCourrier) {
        this.pkCourrier = pkCourrier;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer fkDemandeId) {
        this.demandeId = fkDemandeId;
    }

    public DemandeStatutDTO getFkStatut() {
        return fkStatut;
    }

    public void setFkStatut(DemandeStatutDTO fkStatut) {
        this.fkStatut = fkStatut;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public Date getDatePrinted() {
        return datePrinted;
    }

    public void setDatePrinted(Date datePrinted) {
        this.datePrinted = datePrinted;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getDemandeIdentifiant() {
        return demandeIdentifiant;
    }

    public void setDemandeIdentifiant(String demandeIdentifiant) {
        this.demandeIdentifiant = demandeIdentifiant;
    }
    
}
