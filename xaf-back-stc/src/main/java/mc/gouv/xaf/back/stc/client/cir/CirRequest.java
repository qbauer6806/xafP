package mc.gouv.xaf.back.stc.client.cir;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CirRequest {

    @JsonProperty("NumTpe")
    public String numTpe;
    @JsonProperty("numPermis")
    public String numPermis;
    @JsonProperty("numImmat")
    public String numImmat;
    @JsonProperty("registre")
    public Integer registre;
    @JsonProperty("dateOperation")
    public String dateOperation;
    @JsonProperty("montant")
    public Double montant;
    @JsonProperty("nomPropr")
    public String nomPropr;
    @JsonProperty("prenomPropr")
    public String prenomPropr;
    @JsonProperty("codeTransaction")
    public String codeTransaction;
    @JsonProperty("autorisation")
    public String autorisation;
    @JsonProperty("transactionId")
    public String transactionId;
    @JsonProperty("codeReglement")
    public String codeReglement;
    @JsonProperty("email")
    public String email;
    @JsonProperty("codeOperation")
    public String codeOperation;
    @JsonProperty("montantOperation")
    public String montantOperation;

    public String getNumTpe() {
        return numTpe;
    }

    public void setNumTpe(String numTpe) {
        this.numTpe = numTpe;
    }

    public String getNumPermis() {
        return numPermis;
    }

    public void setNumPermis(String numPermis) {
        this.numPermis = numPermis;
    }

    public String getNumImmat() {
        return numImmat;
    }

    public void setNumImmat(String numImmat) {
        this.numImmat = numImmat;
    }

    public Integer getRegistre() {
        return registre;
    }

    public void setRegistre(Integer registre) {
        this.registre = registre;
    }

    public String getDateOperation() {
        return dateOperation;
    }

    public void setDateOperation(String dateOperation) {
        this.dateOperation = dateOperation;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public String getNomPropr() {
        return nomPropr;
    }

    public void setNomPropr(String nomPropr) {
        this.nomPropr = nomPropr;
    }

    public String getPrenomPropr() {
        return prenomPropr;
    }

    public void setPrenomPropr(String prenomPropr) {
        this.prenomPropr = prenomPropr;
    }

    public String getCodeTransaction() {
        return codeTransaction;
    }

    public void setCodeTransaction(String codeTransaction) {
        this.codeTransaction = codeTransaction;
    }

    public String getAutorisation() {
        return autorisation;
    }

    public void setAutorisation(String autorisation) {
        this.autorisation = autorisation;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getCodeReglement() {
        return codeReglement;
    }

    public void setCodeReglement(String codeReglement) {
        this.codeReglement = codeReglement;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(String codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getMontantOperation() {
        return montantOperation;
    }

    public void setMontantOperation(String montantOperation) {
        this.montantOperation = montantOperation;
    }

    @Override
    public String toString() {
        return "CirRequest{" +
                "numTpe='" + numTpe + '\'' +
                ", numPermis='" + numPermis + '\'' +
                ", numImmat='" + numImmat + '\'' +
                ", registre=" + registre +
                ", dateOperation='" + dateOperation + '\'' +
                ", montant=" + montant +
                ", nomPropr='" + nomPropr + '\'' +
                ", prenomPropr='" + prenomPropr + '\'' +
                ", codeTransaction='" + codeTransaction + '\'' +
                ", autorisation='" + autorisation + '\'' +
                ", transactionId='" + transactionId + '\'' +
                ", codeReglement='" + codeReglement + '\'' +
                ", email='" + email + '\'' +
                ", codeOperation='" + codeOperation + '\'' +
                ", montantOperation='" + montantOperation + '\'' +
                '}';
    }
}
