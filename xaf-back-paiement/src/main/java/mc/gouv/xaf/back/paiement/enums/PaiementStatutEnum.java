package mc.gouv.xaf.back.paiement.enums;

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

    @Override
    public String toString() {
        return libelle;
    }

}
