package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_COMMANDE")
public class CommandeBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDE", nullable = false)
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
