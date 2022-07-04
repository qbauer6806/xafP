package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_MOYEN_PAIEMENT")
public class MoyenPaiementBO {
    @Id
    @Column(name = "PK_MOYEN_PAIEMENT", nullable = false)
    private String pkMoyenPaiement;

    @OneToOne
    @JoinColumn(name = "FK_COMMANDE")
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

    public String getPkMoyenPaiement() {
        return pkMoyenPaiement;
    }

    public void setPkMoyenPaiement(String reference) {
        this.pkMoyenPaiement = reference;
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
                "reference='" + pkMoyenPaiement + '\'' +
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
