package mc.gouv.xaf.back.data.es.model;

import java.util.Date;

public class FileEsDTO {
    private String name;
    private String url;
    private String meta;
    private String content;
    private String language;
    private String type;
    private String statut;
    private Date dateCreation;
    private Integer pkDemande;
    private Integer pkDemandeFile;
    private String typedoc;
    private Date datePrinted;
    private String identifiant;
    private String identifiantDemande;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Integer getPkDemande() {
        return pkDemande;
    }

    public void setPkDemande(Integer pkDemande) {
        this.pkDemande = pkDemande;
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

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
    }
}
