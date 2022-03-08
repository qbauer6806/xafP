package mc.gouv.xaf.back.data.es.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.StringUtils;
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
    public static final String TYPE_FIELD = "typeFichier";
    public static final String IDENTIFIANT_FIELD = "identifiant";
    public static final String DATE_PRINTED_FIELD = "datePrinted";
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
    private String name;
    @NotNull
    private String url;
    private String meta;
    private String content;
    private String language;
    private String typeFichier;
    private String statut;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date dateCreation;
    private Integer pkDemandes;
    private Integer pkDemandeFile;
    private String typedoc;
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date datePrinted;
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
        this.identifiantDemande = parent;
        setDemandeJoinField(new DemandeJoinFieldEsDTO(INDEX_FILES_JOIN_DOC, parent));
    }

    /**
     * @deprecated les jointures seront supprimées dans ES8
     */
    public DemandeFileEsDTO(String parent, String url) {
        this.identifiantDemande = parent;
        this.url = url;
        if (StringUtils.isNotEmpty(url)) {
            this.identifiant = url.replace('/', '-');
        }
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
