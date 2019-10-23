package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input de WS pour les demandes
 * 
 * @author qdeme
 *
 */
public class DemandeInputDTO {

    private JsonNode contenu;

    private DemandeFileDTO[] fichiers;

    private String langue;

    private DemandeCanalEnum canal;

    private String observations;

    private String agentAffecteId;

    private Date courrierDateReception;

    private String courrierRefInterne;

    private String creeParAgentId;
    
    private boolean novalidate;
    
    private String buildId;
    
    private String recapType;

    public JsonNode getContenu() {
        return contenu;
    }

    public void setContenu(JsonNode contenu) {
        this.contenu = contenu;
    }

    public DemandeFileDTO[] getFichiers() {
        return fichiers;
    }

    public void setFichiers(DemandeFileDTO[] fichiers) {
        this.fichiers = fichiers;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public DemandeCanalEnum getCanal() {
        return canal;
    }

    public void setCanal(DemandeCanalEnum canal) {
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

    public String getCreeParAgentId() {
        return creeParAgentId;
    }

    public void setCreeParAgentId(String creeParAgentId) {
        this.creeParAgentId = creeParAgentId;
    }
    
    public boolean isNovalidate() {
        return novalidate;
    }
    
    public void setNovalidate(boolean novalidate) {
        this.novalidate = novalidate;
    }

	public String getBuildId() {
		return buildId;
	}

	public void setBuildId(String buildId) {
		this.buildId = buildId;
	}

	public String getRecapType() {
		return recapType;
	}

	public void setRecapType(String recapType) {
		this.recapType = recapType;
	}

}
