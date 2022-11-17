package mc.gouv.xaf.back.paiement.data.entity;

import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_COMMANDES_OPERATIONS")
public class CommandeOperationBO {

    @Id
    @Column(name = "PK_OPERATIONS", nullable = false)
    private String pkOperations;

    @Enumerated(EnumType.STRING)
    private OperationTypeEnum operationType;

    @Enumerated(EnumType.STRING)
    private OperationStatutEnum operationStatut;

    @ManyToOne
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    private Double montant;

    private String numeroAutorisation;

    private String numeroFacture;

    private String codeRetour;

    private String libelle;

    public String getCodeRetour() {
        return codeRetour;
    }

    public void setCodeRetour(String codeRetour) {
        this.codeRetour = codeRetour;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getPkOperations() {
        return pkOperations;
    }

    public void setPkOperations(String reference) {
        this.pkOperations = reference;
    }

    public OperationTypeEnum getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypeEnum operationType) {
        this.operationType = operationType;
    }

    public OperationStatutEnum getOperationStatut() {
        return operationStatut;
    }

    public void setOperationStatut(OperationStatutEnum operationStatut) {
        this.operationStatut = operationStatut;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateDerniereModification() {
        return dateDerniereModification;
    }

    public void setDateDerniereModification(LocalDateTime dateDerniereModification) {
        this.dateDerniereModification = dateDerniereModification;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getNumeroAutorisation() {
        return numeroAutorisation;
    }

    public void setNumeroAutorisation(String numeroAuthorisation) {
        this.numeroAutorisation = numeroAuthorisation;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public CommandeBO getCommande() {
        return commande;
    }

    public void setCommande(CommandeBO commande) {
        this.commande = commande;
    }

    @Override
    public String toString() {
        return "OperationBO{" +
                "pkOperation='" + pkOperations + '\'' +
                ", operationType=" + operationType +
                ", operationStatut=" + operationStatut +
                ", dateCreation=" + dateCreation +
                ", dateDerniereModification=" + dateDerniereModification +
                ", montant=" + montant +
                ", numeroAuthorisation=" + numeroAutorisation +
                ", numeroFacture='" + numeroFacture + '\'' +
                '}';
    }
}
