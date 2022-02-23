package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.dto.AbstractDemandeDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

/**
 * Modélise une demande
 */
@Document(indexName = "#{@environment.getProperty('application.name')}", createIndex = false)
public class DemandeEsDTO extends AbstractDemandeDTO {

    public static final String ACCESS_FIELD_NAME = "access";
    public static final String AGENT_FIELD_NAME = "agent";
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public static final String JOIN_FIELD_NAME = "fichiers.demandeJoinField";
    public static final String DATA_FIELD_NAME = "data";

    private DemandeAccessEsDTO access;
    private CanalEsDto canal;
    private UsagerEsDTO usager;
    private AgentEsDTO agent;
    private DemandeStatutEsDTO[] statuts;
    private DemandeStatutEsDTO dernierStatut;
    private String statutPublicOuInterne;
    private JsonNode data;
    private String agentAffecteNomAffichage;
    private List<String> nomsCourriers;
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    private DemandeJoinFieldEsDTO demandeJoinField;
    private List<String> justificatifsTraitement;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDemande;

    @Id
    protected String identifiant;

    public DemandeEsDTO() {
        super();
        setDemandeJoinField(new DemandeJoinFieldEsDTO("demandes"));
    }

    public DemandeEsDTO(String identifiant) {
        setIdentifiant(identifiant);
    }

    public DemandeAccessEsDTO getAccess() {
        return access;
    }

    public void setAccess(DemandeAccessEsDTO access) {
        this.access = access;
    }

    public CanalEsDto getCanal() {
        return canal;
    }

    public void setCanal(CanalEsDto canal) {
        this.canal = canal;
    }

    public UsagerEsDTO getUsager() {
        return usager;
    }

    public void setUsager(UsagerEsDTO usager) {
        this.usager = usager;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public AgentEsDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentEsDTO agent) {
        this.agent = agent;
    }

    public DemandeStatutEsDTO[] getStatuts() {
        return statuts;
    }

    public void setStatuts(DemandeStatutEsDTO[] statuts) {
        this.statuts = statuts;
    }

    public DemandeStatutEsDTO getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(DemandeStatutEsDTO dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public JsonNode getData() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public String getAgentAffecteNomAffichage() {
        return agentAffecteNomAffichage;
    }

    public void setAgentAffecteNomAffichage(String agentAffecteNomAffichage) {
        this.agentAffecteNomAffichage = agentAffecteNomAffichage;
    }

    public List<String> getNomsCourriers() {
        return nomsCourriers;
    }

    public void setNomsCourriers(List<String> nomsCourriers) {
        this.nomsCourriers = nomsCourriers;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public void setDemandeJoinField(DemandeJoinFieldEsDTO demandeJoinField) {
        this.demandeJoinField = demandeJoinField;
    }

    public String getStatutPublicOuInterne() {
        return statutPublicOuInterne;
    }

    public void setStatutPublicOuInterne(String statutPublicOuInterne) {
        this.statutPublicOuInterne = statutPublicOuInterne;
    }

    public List<String> getJustificatifsTraitement() {
        return justificatifsTraitement;
    }

    public void setJustificatifsTraitement(List<String> justificatifsTraitement) {
        this.justificatifsTraitement = justificatifsTraitement;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }
}
