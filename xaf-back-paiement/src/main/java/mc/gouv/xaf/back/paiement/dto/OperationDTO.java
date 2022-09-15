package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OperationDTO {

    private String pkOperation;

    private String operationType;

    private String operationStatut;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    private Double montant;

    private Integer numeroAutorisation;

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

    public String getPkOperation() {
        return pkOperation;
    }

    public void setPkOperation(String reference) {
        this.pkOperation = reference;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationStatut() {
        return operationStatut;
    }

    public void setOperationStatut(String operationStatut) {
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

    public Integer getNumeroAutorisation() {
        return numeroAutorisation;
    }

    public void setNumeroAutorisation(Integer numeroAuthorisation) {
        this.numeroAutorisation = numeroAuthorisation;
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
                "pkOperation='" + pkOperation + '\'' +
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
