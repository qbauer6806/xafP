package mc.gouv.xaf.back.data.es.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class DemandeStatutEsDTO {

    public static final String CODE_FIELD_NAME = "code";

    private String commentaire;
    private String texteAEnvoyer;
    private String libelle;
    private String code;
    private String libelleMotif;
    private String codeMotif;
    
    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    protected Date date;

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

    public String getTexteAEnvoyer() {
        return texteAEnvoyer;
    }

    public void setTexteAEnvoyer(String texteAEnvoyer) {
        this.texteAEnvoyer = texteAEnvoyer;
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
    
    public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

}
