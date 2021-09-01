package mc.gouv.xaf.back.dsp.enums;

public enum ResidQualiteEnum {

    AUTRE("AUTRE"),
    COLOCATAIRE("COLOCATAIRE"),
    COPROPRIETAIRE("COPROPRIETAIRE"),
    HEBERGE("HEBERGE"),
    LOCATAIRE("LOCATAIRE"),
    MISE_A_DISPOSITION("MISE_A_DISPOSITION"),
    NU_PROPRIETAIRE("NU_PROPRIETAIRE"),
    PENSIONNAIRE("PENSIONNAIRE"),
    PROPRIETAIRE("PROPRIETAIRE"),
    USUFRUITIER("USUFRUITIER");

    public String value;

    ResidQualiteEnum(String value) {
        this.value = value;
    }
}
