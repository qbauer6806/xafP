package mc.gouv.af.back.data.es.model;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Parent;

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
    public static final String INDEX_FILES_TYPE = "fichiers";
    public static final String ACCESS_FIELD_NAME = "access";
    public static final String AGENT_FIELD_NAME = "agent";

    private DemandeAccessEsDTO access;
    private CanalEsDto canal;
    private UsagerEsDTO usager;
    private AgentEsDTO agent;
    private DemandeStatutEsDTO[] statuts;
    private DemandeStatutEsDTO dernierStatut;
    private JsonNode data;
    private String agentAffecteNomAffichage;
    private List<String> nomsCourriers;

    @Id
    protected String identifiant;

    public DemandeEsDTO() {
        super();
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

    @Document(indexName = "#{propertiesResolver.indexAlias}", type = DemandeEsDTO.INDEX_FILES_TYPE, createIndex = false)
    public static class DemandeFileEsDTO {

        public enum TYPE {
            PIECE_JOINTE,
            COMPLEMENT
        }

        public static final String TYPE_FIELD = "type";

        @NotNull
        protected String name;

        @Id
        protected String id;

        @NotNull
        protected String url;
        protected String meta;
        private String content;
        private String language;
        private String type;

        @Parent(type = INDEX_TYPE)
        private String demandeId;

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

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }

        public String getDemandeId() {
            return demandeId;
        }

        public void setDemandeId(String demandeId) {
            this.demandeId = demandeId;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return url.replace("/", "-").replace("\\", "-");
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

    }

}
