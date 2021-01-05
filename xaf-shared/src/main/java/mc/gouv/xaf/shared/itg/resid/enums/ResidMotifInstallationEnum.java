package mc.gouv.xaf.shared.itg.resid.enums;

public enum ResidMotifInstallationEnum {

    CADRE_DE_VIE("CADRE_DE_VIE"),
    FAMILIAL("FAMILIAL"),
    FISCAL("FISCAL"),
    PROFESSIONNEL("PROFESSIONNEL"),
    REFUGIE("REFUGIE"),
    SECURITE("SECURITE"),
    SANS_MOTIVATION_PRINCIPALE("SANS_MOTIVATION_PRINCIPALE"),
    A_DETERMINER("A_DETERMINER");

    public String value;

    ResidMotifInstallationEnum(String value) {
        this.value = value;
    }

}
