package mc.gouv.xaf.back.paiement.enums;

public enum StatutDebitEnum {

    PAID("PAID"),
    UNPAID("UNPAID"),
    PENDING("PENDING");

    private final String libelle;

    StatutDebitEnum(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return libelle;
    }
}
