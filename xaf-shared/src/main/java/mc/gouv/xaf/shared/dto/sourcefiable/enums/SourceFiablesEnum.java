package mc.gouv.xaf.shared.dto.sourcefiable.enums;

public enum SourceFiablesEnum {

    DSP_RESID("DSP-RESID", "DSP", "RESID"),
    MAIRIE("Mairie-Identité numérique", "Autorité d’enregistrement (DSP ou Marie ou DSN)", "Mroad"),
    MCONNECT("Donnée certifiée", "DSN", "TS");

    private final String libelle;
    private final String service;
    private final String application;

    SourceFiablesEnum(String libelle, String service, String application) {
        this.libelle = libelle;
        this.service = service;
        this.application = application;
    }
    public String getService() {
        return service;
    }
    public String getApplication() {
        return application;
    }
    @Override
    public String toString() {
        return libelle;
    }


}
