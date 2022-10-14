package mc.gouv.xaf.back.paiement.enums;

public enum PaiementStatutEnum {
    EMPREINTE_VALIDE("Empreinte bancaire valide"),
    EMPREINTE_EXPIREE("Empreinte bancaire expirée"),
    DEBIT_ECHEC("Débit en échec"),
    DEBIT_ABANDONNE("Débit abandonné"),
    DEBIT_REALISE("Débit réalisé");

    private final String libelle;

    PaiementStatutEnum(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
