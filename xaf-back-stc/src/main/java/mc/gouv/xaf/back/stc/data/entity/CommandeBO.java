package mc.gouv.xaf.back.stc.data.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class CommandeBO {
    @Id
    @GeneratedValue
    private Long id;

    private LocalDateTime dateCreation;

    private double montant;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
                "id=" + id +
                ", dateCreation=" + dateCreation +
                ", montant=" + montant +
                '}';
    }
}
