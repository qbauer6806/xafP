package mc.gouv.af.back.data.es.model;

public class DemandeStatutEsDTO {

    public static final String CODE_FIELD_NAME = "code";

    private String commentaire;
    private String libelle;
    private String code;
    private String libelleMotif;
    private String codeMotif;

    public String getLibelleMotif() {
        return libelleMotif;
    }

    public void setLibelleMotif(String libelleMotif) {
        this.libelleMotif = libelleMotif;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getCodeMotif() {
        return codeMotif;
    }

    public void setCodeMotif(String codeMotif) {
        this.codeMotif = codeMotif;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
