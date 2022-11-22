package mc.gouv.xaf.back.paiement.dto;

public class CommandeDemandeArticleDTO {
    private Integer pkCommandesDemandesArticles;

    private Integer fkCommandeDemande;

    private String codeTarif;

    private double montant;

    public Integer getPkCommandesDemandesArticles() {
        return pkCommandesDemandesArticles;
    }

    public void setPkCommandesDemandesArticles(Integer pkCommandesDemandesArticles) {
        this.pkCommandesDemandesArticles = pkCommandesDemandesArticles;
    }

    public Integer getFkCommandeDemande() {
        return fkCommandeDemande;
    }

    public void setFkCommandeDemande(Integer fkCommandeDemande) {
        this.fkCommandeDemande = fkCommandeDemande;
    }

    public String getCodeTarif() {
        return codeTarif;
    }

    public void setCodeTarif(String codeTarif) {
        this.codeTarif = codeTarif;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }
}
