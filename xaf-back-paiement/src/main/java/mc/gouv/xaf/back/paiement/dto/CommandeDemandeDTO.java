package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeDemandeDTO {

    private Integer pkCommandeDemandes;

    private Double montant;

    public Integer getPkCommandeDemandes() {
        return pkCommandeDemandes;
    }

    public void setPkCommandeDemandes(Integer pkCommandeDemandes) {
        this.pkCommandeDemandes = pkCommandeDemandes;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }
}
