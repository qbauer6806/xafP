package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(indexName = "#{@environment.getProperty('application.name')}", createIndex = false)
public class DemandeFileEsDTO {

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public static final String INDEX_FILES_JOIN_DOC = "fichiers";
    public static final String TYPE_FIELD = "fichierType";
    public static final String IDENTIFIANT_FIELD = "identifiant";
    public static final String DATE_PRINTED_FIELD = "fichierDatePrinted";
    /**
     * Id unique, différent de la pkDemandeFile (généré à partir de l'url et nom) et utilisé par ES
     */
    @Id
    private String identifiant;
    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    private DemandeJoinFieldEsDTO demandeJoinField;
    @NotNull
    private String fichierName;
    @NotNull
    private String fichierUrl;
    private String fichierMeta;
    private String fichierContent;
    private String fichierLanguage;
    private String fichierType;
    private String fichierStatut;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    private Integer pkDemandes;
    private Integer fichierPkDemandeFile;
    private String fichierTypedoc;
    private Date fichierDatePrinted;
    /**
     * TODO Identifiant courrier (ref_interne sur la page gestioncourrier)
     */
    private String identifiantFichier;
    /**
     * Identifiant de la demande
     */
    private String identifiantDemande;

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public DemandeFileEsDTO(String parent) {
        setDemandeJoinField(new DemandeJoinFieldEsDTO(INDEX_FILES_JOIN_DOC, parent));
    }

    public DemandeFileEsDTO() {

    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public DemandeJoinFieldEsDTO getDemandeJoinField() {
        return demandeJoinField;
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public void setDemandeJoinField(DemandeJoinFieldEsDTO demandeJoinField) {
        this.demandeJoinField = demandeJoinField;
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

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
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

    public String getIdentifiantFichier() {
        return identifiantFichier;
    }

    public void setIdentifiantFichier(String identifiantFichier) {
        this.identifiantFichier = identifiantFichier;
    }

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
    }

    public enum TYPE {
        PIECE_JOINTE,
        FICHIER_INTERNE,
        COMPLEMENT,
        COURRIER
    }

}
