package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "PMNT_COMMANDES")
public class CommandeBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDES", nullable = false)
    private Integer pkCommandes;

    private LocalDateTime dateCreation;

    private double montantInitial;

    private double montantDejaCapture;

    private double montantRestant;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private MoyenPaiementBO moyenPaiement;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeDemandeBO> commandesDemandes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeOperationBO> operations;

    public Integer getPkCommandes() {
        return pkCommandes;
    }

    public void setPkCommandes(Integer id) {
        this.pkCommandes = id;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public double getMontantInitial() {
        return montantInitial;
    }

    public void setMontantInitial(double montantInitial) {
        this.montantInitial = montantInitial;
    }

    public double getMontantDejaCapture() {
        return montantDejaCapture;
    }

    public void setMontantDejaCapture(double montantCapture) {
        this.montantDejaCapture = montantCapture;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(double montantRestant) {
        this.montantRestant = montantRestant;
    }

    public MoyenPaiementBO getMoyenPaiement() {
        return moyenPaiement;
    }

    public void setMoyenPaiement(MoyenPaiementBO moyenPaiement) {
        this.moyenPaiement = moyenPaiement;
    }

    public List<CommandeDemandeBO> getCommandesDemandes() {
        return commandesDemandes;
    }

    public void setCommandesDemandes(List<CommandeDemandeBO> commandeDemandes) {
        this.commandesDemandes = commandeDemandes;
    }

    public List<CommandeOperationBO> getOperations() {
        return operations;
    }

    public void setOperations(List<CommandeOperationBO> operations) {
        this.operations = operations;
    }

    @Override
    public String toString() {
        return "CommandeBO{" +
                "id=" + pkCommandes +
                ", dateCreation=" + dateCreation +
                '}';
    }
}
