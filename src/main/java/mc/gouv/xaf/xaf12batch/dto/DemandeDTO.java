package mc.gouv.xaf.xaf12batch.dto;

public class DemandeDTO {

    private String code;
    private String libelle;

    private Integer pkDemandes;

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
