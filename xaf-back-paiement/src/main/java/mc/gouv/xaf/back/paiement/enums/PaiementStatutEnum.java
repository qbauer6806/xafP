package mc.gouv.xaf.back.paiement.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PaiementStatutEnum {
    EMPREINTE_VALIDE("Empreinte bancaire valide"),
    EMPREINTE_EXPIREE("Empreinte bancaire expirée"),
    CARTE_VALIDE("Carte bancaire valide"),
    DEBIT_ECHEC("Débit en échec"),
    DEBIT_ABANDONNE("Débit abandonné"),
    DEBIT_REALISE("Débit réalisé");

    private final String libelle;

    PaiementStatutEnum(String libelle) {
        this.libelle = libelle;
    }

    @JsonValue
    public String getValue() {
        return name();
    }

    @JsonCreator
    public static PaiementStatutEnum fromValue(String value) {
        for (PaiementStatutEnum e : values()) {
            if (e.name().equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    @Override
    public String toString() {
        return libelle;
    }

}
