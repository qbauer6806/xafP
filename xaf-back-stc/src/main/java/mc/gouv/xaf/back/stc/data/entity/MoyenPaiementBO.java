package mc.gouv.xaf.back.stc.data.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Entity
public class MoyenPaiementBO {
    @Id
    private String reference;

    @OneToOne
    @JoinColumn(name = "commande_id")
    private CommandeBO commande;


    private LocalDateTime dateLimite;

    private double montantInitial;

    private double montantCapture;

    private double montantRestant;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementTypeBO moyenPaiementType;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementStatutBO moyenPaiementStatut;

    private LocalDateTime dateDerniereModification;

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public CommandeBO getCommande() {
        return commande;
    }

    public void setCommande(CommandeBO commande) {
        this.commande = commande;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public double getMontantInitial() {
        return montantInitial;
    }

    public void setMontantInitial(double montantInitial) {
        this.montantInitial = montantInitial;
    }

    public double getMontantCapture() {
        return montantCapture;
    }

    public void setMontantCapture(double montantCapture) {
        this.montantCapture = montantCapture;
    }

    public double getMontantRestant() {
        return montantRestant;
    }

    public void setMontantRestant(double montantRestant) {
        this.montantRestant = montantRestant;
    }

    public MoyenPaiementTypeBO getMoyenPaiementType() {
        return moyenPaiementType;
    }

    public void setMoyenPaiementType(MoyenPaiementTypeBO moyenPaiementType) {
        this.moyenPaiementType = moyenPaiementType;
    }

    public MoyenPaiementStatutBO getMoyenPaiementStatut() {
        return moyenPaiementStatut;
    }

    public void setMoyenPaiementStatut(MoyenPaiementStatutBO moyenPaiementStatut) {
        this.moyenPaiementStatut = moyenPaiementStatut;
    }

    public LocalDateTime getDateDerniereModification() {
        return dateDerniereModification;
    }

    public void setDateDerniereModification(LocalDateTime dateDerniereModification) {
        this.dateDerniereModification = dateDerniereModification;
    }

    @Override
    public String toString() {
        return "MoyenPaiementBO{" +
                "reference='" + reference + '\'' +
                ", commande=" + commande +
                ", dateLimite=" + dateLimite +
                ", montantInitial=" + montantInitial +
                ", montantCapture=" + montantCapture +
                ", montantRestant=" + montantRestant +
                ", moyenPaiementType=" + moyenPaiementType +
                ", moyenPaiementStatut=" + moyenPaiementStatut +
                ", dateDerniereModification=" + dateDerniereModification +
                '}';
    }
}
