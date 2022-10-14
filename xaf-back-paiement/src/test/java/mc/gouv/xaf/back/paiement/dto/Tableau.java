package mc.gouv.xaf.back.paiement.dto;

public class Tableau {
    private String objet;
    private String montant;

    public Tableau(String objet, String montant) {
        this.objet = objet;
        this.montant = montant;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getMontant() {
        return montant;
    }

    public void setMontant(String montant) {
        this.montant = montant;
    }
}
