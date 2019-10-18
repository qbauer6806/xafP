package mc.gouv.xaf.back.data.es.model;

public class CanalEsDto {

    public static final String CANAL_CODE_FIELD_NAME = "code";

    private String code;
    private String libelle;

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

}
