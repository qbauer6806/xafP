package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import mc.gouv.xaf.shared.dto.es.GenericContenuEsDTO;
import mc.gouv.xaf.shared.dto.es.GenericDemandeDataEsDTO;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

/**
 * Modélise une demande
 */
@Document(indexName = "#{@environment.getProperty('application.name')}", createIndex = false)
public class DemandeEsDTO {

    public static final String DERNIER_STATUT_FIELD_NAME = "dernierStatut";
    public static final String CANAL_FIELD_NAME = "canal";
    public static final String ACCESS_FIELD_NAME = "access";
    public static final String AGENT_FIELD_NAME = "agent";
    public static final String DATA_FIELD_NAME = "data";
    public static final String DATE_DEMANDE_FIELD_NAME = "dateDemande";
    public static final String IDENTIFIANT_FIELD_NAME = "identifiant";

    private DemandeAccessEsDTO access;
    private AgentEsDTO agent;
    private String agentAffecteId;
    private String agentAffecteNomAffichage;
    private CanalEsDto canal;
    private GenericContenuEsDTO contenu;
    private Date courrierDateReception;
    private String courrierRefInterne;
    private String creeParAgentId;
    private GenericDemandeDataEsDTO data;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDemande;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    @Deprecated(forRemoval = true)
    private DemandeJoinFieldEsDTO demandeJoinField;
    private DemandeStatutEsDTO dernierStatut;
    @Id
    private String identifiant;
    private String langue;
    private List<String> nomsCourriers;
    private String observations;
    private Integer pkDemandes;
    private String statutPublicOuInterne;
    private UsagerEsDTO usager;
    private DemandeStatutEsDTO[] statuts;
    private List<String> justificatifsTraitement;
    private Long modificationTimestamp;

    public Long getModificationTimestamp() {
        return modificationTimestamp;
    }

    public void setModificationTimestamp(Long modificationTimestamp) {
        this.modificationTimestamp = modificationTimestamp;
    }

    @JsonIgnore
    protected boolean updated = false;

    // Champs pour récupérer les fichiers dans un DemandeEsRechercheDTO
    private String name;
    private String url;
    private String meta;
    private String content;
    private String language;
    private String typeFichier;
    private String statut;
    private Integer pkDemandeFile;
    private String typedoc;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date datePrinted;

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

    public AgentEsDTO getAgent() {
        return agent;
    }

    public void setAgent(AgentEsDTO agent) {
        this.agent = agent;
    }

    public String getAgentAffecteId() {
        return agentAffecteId;
    }

    public void setAgentAffecteId(String agentAffecteId) {
        this.agentAffecteId = agentAffecteId;
    }

    public String getAgentAffecteNomAffichage() {
        return agentAffecteNomAffichage;
    }

    public void setAgentAffecteNomAffichage(String agentAffecteNomAffichage) {
        this.agentAffecteNomAffichage = agentAffecteNomAffichage;
    }

    public CanalEsDto getCanal() {
        return canal;
    }

    public void setCanal(CanalEsDto canal) {
        this.canal = canal;
    }

    public GenericContenuEsDTO getContenu() {
        return contenu;
    }

    public void setContenu(GenericContenuEsDTO contenu) {
        this.contenu = contenu;
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

    public GenericDemandeDataEsDTO getData() {
        return data;
    }

    public void setData(GenericDemandeDataEsDTO data) {
        this.data = data;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Date getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(Date dateDemande) {
        this.dateDemande = dateDemande;
    }

    public Date getDateDerModif() {
        return dateDerModif;
    }

    public void setDateDerModif(Date dateDerModif) {
        this.dateDerModif = dateDerModif;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    @Deprecated(forRemoval = true)
    public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    @Deprecated(forRemoval = true)
    public void setDemandeJoinField(DemandeJoinFieldEsDTO demandeJoinField) {
        this.demandeJoinField = demandeJoinField;
    }

    public DemandeStatutEsDTO getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(DemandeStatutEsDTO dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public List<String> getNomsCourriers() {
        return nomsCourriers;
    }

    public void setNomsCourriers(List<String> nomsCourriers) {
        this.nomsCourriers = nomsCourriers;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

    public String getStatutPublicOuInterne() {
        return statutPublicOuInterne;
    }

    public void setStatutPublicOuInterne(String statutPublicOuInterne) {
        this.statutPublicOuInterne = statutPublicOuInterne;
    }

    public UsagerEsDTO getUsager() {
        return usager;
    }

    public void setUsager(UsagerEsDTO usager) {
        this.usager = usager;
    }

    public DemandeStatutEsDTO[] getStatuts() {
        return statuts;
    }

    public void setStatuts(DemandeStatutEsDTO[] statuts) {
        this.statuts = statuts;
    }

    public List<String> getJustificatifsTraitement() {
        return justificatifsTraitement;
    }

    public void setJustificatifsTraitement(List<String> justificatifsTraitement) {
        this.justificatifsTraitement = justificatifsTraitement;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
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

    public String getTypeFichier() {
        return typeFichier;
    }

    public void setTypeFichier(String typeFichier) {
        this.typeFichier = typeFichier;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Integer getPkDemandeFile() {
        return pkDemandeFile;
    }

    public void setPkDemandeFile(Integer pkDemandeFile) {
        this.pkDemandeFile = pkDemandeFile;
    }

    public String getTypedoc() {
        return typedoc;
    }

    public void setTypedoc(String typedoc) {
        this.typedoc = typedoc;
    }

    public Date getDatePrinted() {
        return datePrinted;
    }

    public void setDatePrinted(Date datePrinted) {
        this.datePrinted = datePrinted;
    }
}
