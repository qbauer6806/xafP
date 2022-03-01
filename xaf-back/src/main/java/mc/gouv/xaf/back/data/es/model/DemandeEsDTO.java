package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * Modélise une demande
 */
@Document(indexName = "#{@environment.getProperty('application.name')}", createIndex = false)
public class DemandeEsDTO /* TODO extends AbstractDemandeDTO*/ {

    public static final String DERNIER_STATUT_FIELD_NAME = "dernierStatut";
    public static final String CANAL_FIELD_NAME = "canal";
    public static final String ACCESS_FIELD_NAME = "access";
    public static final String AGENT_FIELD_NAME = "agent";
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public static final String JOIN_FIELD_NAME = "fichiers.demandeJoinField";
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
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDemande;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateDerModif;
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
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

    // TODO private DemandeStatutEsDTO[] statuts;
    // TODO private JsonNode data;
    // TODO private List<String> justificatifsTraitement;

    // Fichiers
    private String fichierName;
    private String fichierUrl;
    private String fichierMeta;
    private String fichierContent;
    private String fichierLanguage;
    private String fichierType;
    private String fichierStatut;
    private Integer fichierPkDemandeFile;
    private String fichierTypedoc;
    private Date fichierDatePrinted;

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
    public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
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

    public String getFichierName() {
        return fichierName;
    }

    public void setFichierName(String fichierName) {
        this.fichierName = fichierName;
    }

    public String getFichierUrl() {
        return fichierUrl;
    }

    public void setFichierUrl(String fichierUrl) {
        this.fichierUrl = fichierUrl;
    }

    public String getFichierMeta() {
        return fichierMeta;
    }

    public void setFichierMeta(String fichierMeta) {
        this.fichierMeta = fichierMeta;
    }

    public String getFichierContent() {
        return fichierContent;
    }

    public void setFichierContent(String fichierContent) {
        this.fichierContent = fichierContent;
    }

    public String getFichierLanguage() {
        return fichierLanguage;
    }

    public void setFichierLanguage(String fichierLanguage) {
        this.fichierLanguage = fichierLanguage;
    }

    public String getFichierType() {
        return fichierType;
    }

    public void setFichierType(String fichierType) {
        this.fichierType = fichierType;
    }

    public String getFichierStatut() {
        return fichierStatut;
    }

    public void setFichierStatut(String fichierStatut) {
        this.fichierStatut = fichierStatut;
    }

    public Integer getFichierPkDemandeFile() {
        return fichierPkDemandeFile;
    }

    public void setFichierPkDemandeFile(Integer fichierPkDemandeFile) {
        this.fichierPkDemandeFile = fichierPkDemandeFile;
    }

    public String getFichierTypedoc() {
        return fichierTypedoc;
    }

    public void setFichierTypedoc(String fichierTypedoc) {
        this.fichierTypedoc = fichierTypedoc;
    }

    public Date getFichierDatePrinted() {
        return fichierDatePrinted;
    }

    public void setFichierDatePrinted(Date fichierDatePrinted) {
        this.fichierDatePrinted = fichierDatePrinted;
    }
}
