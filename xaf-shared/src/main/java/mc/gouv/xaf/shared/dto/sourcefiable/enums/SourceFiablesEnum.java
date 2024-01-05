package mc.gouv.xaf.shared.dto.sourcefiable.enums;

public enum SourceFiablesEnum {

    DSP_RESID("DSP-RESID"),
    MAIRIE("Mairie-Identité numérique"),
    MCONNECT("Donnée certifiée");

    private String libelle;

    SourceFiablesEnum(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }



}
