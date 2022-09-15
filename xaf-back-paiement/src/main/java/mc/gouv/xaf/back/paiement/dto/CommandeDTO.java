package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeDTO {
    private Integer pkCommande;

    private LocalDateTime dateCreation;

    private double montant;

    public Integer getPkCommande() {
        return pkCommande;
    }

    public void setPkCommande(Integer id) {
        this.pkCommande = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "CommandeBO{" +
                "id=" + pkCommande +
                ", dateCreation=" + dateCreation +
                ", montant=" + montant +
                '}';
    }
}
