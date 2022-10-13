package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeDemandeDTO {

    private Integer pkCommandeDemandes;

    private Integer fkCommandes;

    private Integer fkDemandes;

    private Double montant;

    private List<CommandeDemandeArticleDTO> commandeDemandeArticles;

    public Integer getPkCommandeDemandes() {
        return pkCommandeDemandes;
    }

    public void setPkCommandeDemandes(Integer pkCommandeDemandes) {
        this.pkCommandeDemandes = pkCommandeDemandes;
    }

    public Integer getFkCommandes() {
        return fkCommandes;
    }

    public void setFkCommandes(Integer fkCommandes) {
        this.fkCommandes = fkCommandes;
    }

    public Integer getFkDemandes() {
        return fkDemandes;
    }

    public void setFkDemandes(Integer fkDemandes) {
        this.fkDemandes = fkDemandes;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public List<CommandeDemandeArticleDTO> getCommandeDemandeArticles() {
        return commandeDemandeArticles;
    }

    public void setCommandeDemandeArticles(List<CommandeDemandeArticleDTO> commandeDemandeArticles) {
        this.commandeDemandeArticles = commandeDemandeArticles;
    }
}
