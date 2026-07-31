package mc.gouv.xaf.shared.dto.sourcefiable.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum SourceFiablesEnum {

    DSP("DSP - Identité numérique", "Autorité d’enregistrement DSP", Constants.IDENTITE_NUMERIQUE),
    DSP_RESID("DSP-RESID", "DSP", "RESID"), // pour les données récupérées de RESID
    MAIRIE("Mairie - Identité numérique", "Autorité d’enregistrement Mairie", Constants.IDENTITE_NUMERIQUE),
    DSN("DSN - Identité numérique", "Autorité d’enregistrement DSN", Constants.IDENTITE_NUMERIQUE),
    MCONNECT("Donnée certifiée", "N/A", "N/A");

    private final String libelle;
    private final String service;
    private final String application;

    SourceFiablesEnum(String libelle, String service, String application) {
        this.libelle = libelle;
        this.service = service;
        this.application = application;
    }

    @Override
    public String toString() {
        return libelle;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static SourceFiablesEnum fromValue(String value) {
        for (SourceFiablesEnum e : values()) {
            if (e.name().equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    private static class Constants {

        public static final String IDENTITE_NUMERIQUE = "Identité Numérique";
    }
}
