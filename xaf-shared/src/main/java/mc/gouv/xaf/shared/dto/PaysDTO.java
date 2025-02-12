package mc.gouv.xaf.shared.dto;

/** 
 * DTO représentant un pays
 * 
 * @author qdeme
 */
public class PaysDTO {

    private String code;

    private String libelle;

    private String libelleEn;

    private String libelleLong;

    private String libelleLongEn;

    private Integer ordre;

    private String nationalite;

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

    public String getLibelleLong() {
        return libelleLong;
    }

    public void setLibelleLong(String libelleLong) {
        this.libelleLong = libelleLong;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public String getNationalite() {
        return nationalite;
    }

    public void setNationalite(String nationalite) {
        this.nationalite = nationalite;
    }

    public String getLibelleEn() {
        return libelleEn;
    }

    public void setLibelleEn(String libelleEn) {
        this.libelleEn = libelleEn;
    }

    public String getLibelleLongEn() {
        return libelleLongEn;
    }

    public void setLibelleLongEn(String libelleLongEn) {
        this.libelleLongEn = libelleLongEn;
    }

}
