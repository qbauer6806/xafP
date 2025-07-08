package mc.gouv.xaf.back.paiement.service.kafka;

public enum PaymentTypeEnum {

    DEMANDE("DEMANDE"),
    COMMANDE("COMMANDE"),
    FACTURE("FACTURE");
    private final String value;

    PaymentTypeEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
