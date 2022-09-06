package mc.gouv.xaf.shared.dto;

import java.util.Arrays;
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
    
    private Integer brouillonId;
    
    // Données envoyées à l'API si l'usager s'est connecté via MConnect
    private DonneesMConnectDTO donneesMConnect;
    
    // En cas de renouvellement d'une demande
    private Integer demandeSourceId;

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

	public Integer getBrouillonId() {
		return brouillonId;
	}

	public void setBrouillonId(Integer brouillonId) {
		this.brouillonId = brouillonId;
	}

	public DonneesMConnectDTO getDonneesMConnect() {
		return donneesMConnect;
	}

	public void setDonneesMConnect(DonneesMConnectDTO donneesMConnect) {
		this.donneesMConnect = donneesMConnect;
	}
	
    public Integer getDemandeSourceId() {
		return demandeSourceId;
	}

	public void setDemandeSourceId(Integer demandeSourceId) {
		this.demandeSourceId = demandeSourceId;
	}

	@Override
	public String toString() {
		return "DemandeInputDTO [contenu=" + contenu + ", fichiers=" + Arrays.toString(fichiers) + ", langue=" + langue
				+ ", canal=" + canal + ", observations=" + observations + ", agentAffecteId=" + agentAffecteId
				+ ", courrierDateReception=" + courrierDateReception + ", courrierRefInterne=" + courrierRefInterne
				+ ", creeParAgentId=" + creeParAgentId + ", novalidate=" + novalidate + ", buildId=" + buildId
				+ ", recapType=" + recapType + ", brouillonId=" + brouillonId + ", donneesMConnect=" + donneesMConnect
				+ ", demandeSourceId=" + demandeSourceId + "]";
	}

}
