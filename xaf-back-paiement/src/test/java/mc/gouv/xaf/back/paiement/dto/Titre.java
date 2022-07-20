package mc.gouv.xaf.back.paiement.dto;

public class Titre {

    public Titre(String numeropermis) {
        this.numeropermis = numeropermis;
    }

    private String numeropermis;

    public String getNumeropermis() {
        return numeropermis;
    }

    public void setNumeropermis(String numeropermis) {
        this.numeropermis = numeropermis;
    }
}
