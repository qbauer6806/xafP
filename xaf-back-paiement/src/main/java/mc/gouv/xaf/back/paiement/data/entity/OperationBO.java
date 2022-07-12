package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_OPERATION")
public class OperationBO {

    @Id
    @Column(name = "PK_OPERATION", nullable = false)
    private String pkOperation;

    @Enumerated(EnumType.STRING)
    private OperationTypeBO operationType;

    @Enumerated(EnumType.STRING)
    private OperationStatutBO operationStatut;


    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;

    private Double montant;
    private Integer numeroAuthorisation;

    private String numeroFacture;


    public String getPkOperation() {
        return pkOperation;
    }

    public void setPkOperation(String reference) {
        this.pkOperation = reference;
    }

    public OperationTypeBO getOperationType() {
        return operationType;
    }

    public void setOperationType(OperationTypeBO operationType) {
        this.operationType = operationType;
    }

    public OperationStatutBO getOperationStatut() {
        return operationStatut;
    }

    public void setOperationStatut(OperationStatutBO operationStatut) {
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

    public Integer getNumeroAuthorisation() {
        return numeroAuthorisation;
    }

    public void setNumeroAuthorisation(Integer numeroAuthorisation) {
        this.numeroAuthorisation = numeroAuthorisation;
    }

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    @Override
    public String toString() {
        return "OperationBO{" +
                "reference='" + pkOperation + '\'' +
                ", operationType=" + operationType +
                ", operationStatut=" + operationStatut +
                ", dateCreation=" + dateCreation +
                ", dateDerniereModification=" + dateDerniereModification +
                ", montant=" + montant +
                ", numeroAuthorisation=" + numeroAuthorisation +
                '}';
    }
}
