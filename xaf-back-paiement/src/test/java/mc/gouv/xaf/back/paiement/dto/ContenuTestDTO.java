package mc.gouv.xaf.back.paiement.dto;

public class ContenuTestDTO {

    private Titre titre;
    private Paiement paiement;

    public Titre getTitre() {
        return titre;
    }

    public void setTitre(Titre titre) {
        this.titre = titre;
    }

    public Paiement getPaiement() {
        return paiement;
    }

    public void setPaiement(Paiement paiement) {
        this.paiement = paiement;
    }
}
