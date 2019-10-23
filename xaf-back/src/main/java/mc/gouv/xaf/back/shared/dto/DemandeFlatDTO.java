package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Modélise une demande simplifiée, à plat, à destination notamment de l'export Excel
 * 
 * @author qdeme
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeFlatDTO {
    
    private Integer pkDemandes;
    
    private Integer usagerId;
    
    private String usagerPrenom;
    
    private String usagerNom;
    
    private String usagerEmail;

    private Date dateCreation;

    private String langue;

    private String canal;

    private String observations;

    private String agentAffecteId;
    
    private String agentAffecteNom;

    private String dernierStatut;

    private String identifiant;

    private Date courrierDateReception;

    private String courrierRefInterne;

    private String motif;

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }
    
    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }
    
    public String getLangue() {
        return langue;
    }
    
    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
    
    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }
    
    public String getAgentAffecteId() {
        return agentAffecteId;
    }

    public void setAgentAffecteId(String agentAffecteId) {
        this.agentAffecteId = agentAffecteId;
    }
    
    public String getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(String dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }
    
    public Date getCourrierDateReception() {
        return courrierDateReception;
    }

    public void setCourrierDateReception(Date courrierDateReception) {
        this.courrierDateReception = courrierDateReception;
    }

    public String getCourrierRefInterne() {
        return courrierRefInterne;
    }
    
    public void setCourrierRefInterne(String courrierRefInterne) {
        this.courrierRefInterne = courrierRefInterne;
    }

    public String getUsagerPrenom() {
        return usagerPrenom;
    }

    public void setUsagerPrenom(String usagerPrenom) {
        this.usagerPrenom = usagerPrenom;
    }
    
    public String getUsagerNom() {
        return usagerNom;
    }
    
    public void setUsagerNom(String usagerNom) {
        this.usagerNom = usagerNom;
    }
    
    public String getUsagerEmail() {
        return usagerEmail;
    }
    
    public void setUsagerEmail(String usagerEmail) {
        this.usagerEmail = usagerEmail;
    }

    public String getAgentAffecteNom() {
        return agentAffecteNom;
    }

    public void setAgentAffecteNom(String agentAffecteNom) {
        this.agentAffecteNom = agentAffecteNom;
    }

    public String getMotif() {
        return motif;
    }

    public void setMotif(String motif) {
        this.motif = motif;
    }
}
