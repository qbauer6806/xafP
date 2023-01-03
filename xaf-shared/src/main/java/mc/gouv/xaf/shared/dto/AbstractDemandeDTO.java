package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class AbstractDemandeDTO {

    public static final String OBSERVATION_FIELD_NAME = "observations";
    public static final String COURRIER_REF_INTERNE_FIELD_NAME = "courrierRefInterne";
    public static final String DEMARCHE_ID_FIELD_NAME = "demarcheId";
    public static final String USAGER_ID_FIELD_NAME = "usagerId";
    public static final String DATE_CREATION_FIELD_NAME = "dateCreation";
    public static final String PK_DEMANDE_FIELD_NAME = "pkDemandes";

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateCreation;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date dateDerModif;

    protected JsonNode contenu;

    protected String langue;

    protected String observations;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date courrierDateReception;

    protected String courrierRefInterne;

    protected String agentAffecteId;

    protected Integer pkDemandes;

    @JsonIgnore
    protected boolean updated = false;

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

    public JsonNode getContenu() {
        return contenu;
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

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
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

    public String getAgentAffecteId() {
        return agentAffecteId;
    }

    public void setAgentAffecteId(String agentAffecteId) {
        this.agentAffecteId = agentAffecteId;
    }

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

}
