package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CommandeOperationDTO {

    private String pkOperations;
    
    private Integer fkCommandes;

    private String operationType;

    private String operationStatut;

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
    
    public Integer getFkCommandes() {
        return fkCommandes;
    }

    public void setFkCommandes(Integer fkCommandes) {
        this.fkCommandes = fkCommandes;
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
