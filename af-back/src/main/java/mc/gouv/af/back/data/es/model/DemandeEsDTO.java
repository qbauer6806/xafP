package mc.gouv.af.back.data.es.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import com.fasterxml.jackson.databind.JsonNode;

import mc.gouv.dem.shared.model.AbstractDemandeDTO;

/**
 * Modélise une demande
 * 
 *
 */
@Document(indexName = "#{propertiesResolver.indexAlias}", type = DemandeEsDTO.INDEX_TYPE, createIndex = false)
public class DemandeEsDTO extends AbstractDemandeDTO {

    public static final String INDEX_TYPE = "demandes";

    public static final String ACCESS_FIELD_NAME = "access";
    public static final String AGENT_FIELD_NAME = "agent";
    public static final String JOIN_FIELD_NAME = "fichiers.demandeJoinField";
    public static final String DATA_FIELD_NAME = "data";

    private DemandeAccessEsDTO access;
    private CanalEsDto canal;
    private UsagerEsDTO usager;
    private AgentEsDTO agent;
    private DemandeStatutEsDTO[] statuts;
    private DemandeStatutEsDTO dernierStatut;
    private JsonNode data;
    private String agentAffecteNomAffichage;
    private List<String> nomsCourriers;
    private DemandeJoinFieldEsDTO demandeJoinField;

    @Id
    protected String identifiant;

    public DemandeEsDTO() {
        super();
        setDemandeJoinField(new DemandeJoinFieldEsDTO(INDEX_TYPE));
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

    public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    public void setDemandeJoinField(DemandeJoinFieldEsDTO demandeJoinField) {
        this.demandeJoinField = demandeJoinField;
    }

}
