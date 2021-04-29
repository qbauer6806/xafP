package mc.gouv.xaf.back.dsp.dto;

import java.io.Serializable;

public class ResidWarningDTO implements Serializable {

	private static final long serialVersionUID = 1016532880273751925L;
	
	private String code;

    private String libelle;

    private String nom;

    private String clef;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getClef() {
        return clef;
    }

    public void setClef(String clef) {
        this.clef = clef;
    }

    @Override
    public String toString() {
        return "ResidWarningDTO{" +
                "code='" + code + '\'' +
                ", libelle='" + libelle + '\'' +
                ", nom='" + nom + '\'' +
                ", clef='" + clef + '\'' +
                '}';
    }

}
