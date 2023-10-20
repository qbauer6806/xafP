package mc.gouv.xaf.shared.dto;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Modélise une demande
 *
 * @author qdeme
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeDTO extends AbstractDemandeDTO {

    protected DemandeCanalEnum canal;
    protected String creeParAgentId;
    protected DemandeCourrierDTO[] courriers;
    private Integer fkAccess;
    private Integer usagerId;
    private String demarcheId;
    private DemandeFileDTO[] fichiers;
    private String identifiant;
    private DemandeStatutDTO[] statuts;
    private DemandeStatutDTO dernierStatut;
    private DemandeDataDTO[] data;
    private DemandeComplementsDTO[] complements;
    private String usagerNom;
    private String usagerPrenom;
    private String usagerEmail;
    private String buildId;
    private String recapType;
    private DonneesMConnectDTO donneesMConnect;
    private String donneesCertifiees;
    private Integer pkDemandeSource;
    private Long modificationTimestamp;
    private JsonNode contenuInitial;
    private JsonNode meta;
    
    public Long getModificationTimestamp() {
        return modificationTimestamp;
    }

    public void setModificationTimestamp(Long modificationTimestamp) {
        this.modificationTimestamp = modificationTimestamp;
    }

    public Integer getFkAccess() {
        return fkAccess;
    }

    public void setFkAccess(Integer fkAccess) {
        this.fkAccess = fkAccess;
    }

    public Integer getUsagerId() {
        return usagerId;
    }

    public void setUsagerId(Integer usagerId) {
        this.usagerId = usagerId;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public DemandeCanalEnum getCanal() {
        return canal;
    }

    public void setCanal(DemandeCanalEnum canal) {
        this.canal = canal;
    }

    public DemandeFileDTO[] getFichiers() {
        return fichiers;
    }

    public void setFichiers(DemandeFileDTO[] fichiers) {
        this.fichiers = fichiers;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public DemandeStatutDTO[] getStatuts() {
        return statuts;
    }

    public void setStatuts(DemandeStatutDTO[] statuts) {
        this.statuts = statuts;
    }

    public DemandeStatutDTO getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(DemandeStatutDTO dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public DemandeDataDTO[] getData() {
        return data;
    }

    public void setData(DemandeDataDTO[] data) {
        this.data = data;
    }

    public DemandeComplementsDTO[] getComplements() {
        return complements;
    }

    public void setComplements(DemandeComplementsDTO[] complements) {
        this.complements = complements;
    }

    public String getCreeParAgentId() {
        return creeParAgentId;
    }

    public void setCreeParAgentId(String creeParAgentId) {
        this.creeParAgentId = creeParAgentId;
    }

    public DemandeCourrierDTO[] getCourriers() {
        return courriers;
    }

    public void setCourriers(DemandeCourrierDTO[] courriers) {
        this.courriers = courriers;
    }

    public String getUsagerNom() {
        return usagerNom;
    }

    public void setUsagerNom(String usagerNom) {
        this.usagerNom = usagerNom;
    }

    public String getUsagerPrenom() {
        return usagerPrenom;
    }

    public void setUsagerPrenom(String usagerPrenom) {
        this.usagerPrenom = usagerPrenom;
    }

    public String getUsagerEmail() {
        return usagerEmail;
    }

    public void setUsagerEmail(String usagerEmail) {
        this.usagerEmail = usagerEmail;
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

    public DonneesMConnectDTO getDonneesMConnect() {
		return donneesMConnect;
	}
	public void setDonneesMConnect(DonneesMConnectDTO donneesMConnect) {
		this.donneesMConnect = donneesMConnect;
	}

    public String getDonneesCertifiees() {
        return donneesCertifiees;
    }

    public void setDonneesCertifiees(String donneesCertifiees) {
        this.donneesCertifiees = donneesCertifiees;
    }

    public Integer getPkDemandeSource() {
        return pkDemandeSource;
    }

    public void setPkDemandeSource(Integer pkDemandeSource) {
        this.pkDemandeSource = pkDemandeSource;
    }

    @Override
    public String toString() {
        return "DemandeDTO [pkDemandes=" + pkDemandes + ", dateCreation=" + dateCreation + ", dateDerModif="
                + dateDerModif + ", contenu=" + contenu + ", demarcheId=" + demarcheId + ", usagerId=" + usagerId
                + ", complements=" + Arrays.toString(complements) + ", statuts=" + Arrays.toString(statuts)
                + ", langue=" + langue + ", canal=" + canal + ", observations=" + observations + ", agentAffecteId="
                + agentAffecteId + ", creeParAgentId=" + creeParAgentId + ", dernierStatut=" + dernierStatut
                + ", identifiant=" + identifiant + ", data=" + Arrays.toString(data) + ", courrierDateReception="
                + courrierDateReception + ", courrierRefInterne=" + courrierRefInterne + ", updated=" + updated + "]"
                + ", usagerNom=" + usagerNom + ", usagerPrenom=" + usagerPrenom + ", usagerEmail=" + usagerEmail
                + ", buildId=" + buildId + ", recapType=" + recapType + ", donneesMConnect=" + donneesMConnect
                + ", donneesCertifiees=" + donneesCertifiees + ", pkDemandeSource=" + pkDemandeSource + "]";
    }

    public JsonNode getContenuInitial() {
        return contenuInitial;
    }

    public void setContenuInitial(JsonNode contenuInitial) {
        this.contenuInitial = contenuInitial;
    }

	public JsonNode getMeta() {
		return meta;
	}

	public void setMeta(JsonNode meta) {
		this.meta = meta;
	}

}
