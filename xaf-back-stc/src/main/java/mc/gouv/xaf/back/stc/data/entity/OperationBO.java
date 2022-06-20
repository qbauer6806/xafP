package mc.gouv.xaf.back.stc.data.entity;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class OperationBO {

    @Id
    private String reference;

    @Enumerated(EnumType.STRING)
    private OperationTypeBO operationType;

    @Enumerated(EnumType.STRING)
    private OperationStatutBO operationStatut;


    private LocalDateTime dateCreation;
    private LocalDateTime dateDerniereModification;

    private Double montant;
    private Integer numeroAuthorisation;


    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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

    @Override
    public String toString() {
        return "OperationBO{" +
                "reference='" + reference + '\'' +
                ", operationType=" + operationType +
                ", operationStatut=" + operationStatut +
                ", dateCreation=" + dateCreation +
                ", dateDerniereModification=" + dateDerniereModification +
                ", montant=" + montant +
                ", numeroAuthorisation=" + numeroAuthorisation +
                '}';
    }
}
