#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CalculAideDTO implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8621310861790388330L;

    private String typeUsager;
    private BigDecimal prixBasVehicule;
    private BigDecimal remiseDeduire;
    private BigDecimal montantBatterie;
    private BigDecimal tva;
    private BigDecimal prixTotalVehicule;
    private BigDecimal applicationPourcentage;
    private BigDecimal montantSimule;
    private BigDecimal montantSimulePlus20;
    private BigDecimal montantSimuleMoins20;
    private BigDecimal primeForfaitaire;
    private BigDecimal primeCalcule;
    private BigDecimal emission;
    private BigDecimal puissance;
    private BigDecimal prixTotal;
    private BigDecimal primeTaxi;
    private BigDecimal montantAide;
    private String commentCGD;

    public String getTypeUsager() {
        return typeUsager;
    }

    public void setTypeUsager(String typeUsager) {
        this.typeUsager = typeUsager;
    }

    public BigDecimal getPrixBasVehicule() {
        return prixBasVehicule;
    }

    public void setPrixBasVehicule(BigDecimal prixBasVehicule) {
        this.prixBasVehicule = prixBasVehicule;
    }

    public BigDecimal getRemiseDeduire() {
        return remiseDeduire;
    }

    public void setRemiseDeduire(BigDecimal remiseDeduire) {
        this.remiseDeduire = remiseDeduire;
    }

    public BigDecimal getMontantBatterie() {
        return montantBatterie;
    }

    public void setMontantBatterie(BigDecimal montantBatterie) {
        this.montantBatterie = montantBatterie;
    }

    public BigDecimal getTva() {
        return tva;
    }

    public void setTva(BigDecimal tva) {
        this.tva = tva;
    }

    public BigDecimal getPrixTotalVehicule() {
        return prixTotalVehicule;
    }

    public void setPrixTotalVehicule(BigDecimal prixTotalVehicule) {
        this.prixTotalVehicule = prixTotalVehicule;
    }

    public BigDecimal getApplicationPourcentage() {
        return applicationPourcentage;
    }

    public void setApplicationPourcentage(BigDecimal applicationPourcentage) {
        this.applicationPourcentage = applicationPourcentage;
    }

    public BigDecimal getMontantSimule() {
        return montantSimule;
    }

    public void setMontantSimule(BigDecimal montantCalcul) {
        this.montantSimule = montantCalcul;
    }

    public BigDecimal getMontantSimulePlus20() {
        return montantSimulePlus20;
    }

    public void setMontantSimulePlus20(BigDecimal montantSimulePlus20) {
        this.montantSimulePlus20 = montantSimulePlus20;
    }

    public BigDecimal getMontantSimuleMoins20() {
        return montantSimuleMoins20;
    }

    public void setMontantSimuleMoins20(BigDecimal montantSimuleMoins20) {
        this.montantSimuleMoins20 = montantSimuleMoins20;
    }

    public BigDecimal getEmission() {
        return emission;
    }

    public BigDecimal getPrimeForfaitaire() {
        return primeForfaitaire;
    }

    public void setPrimeForfaitaire(BigDecimal primeForfaitaire) {
        this.primeForfaitaire = primeForfaitaire;
    }

    public BigDecimal getPrimeCalcule() {
        return primeCalcule;
    }

    public void setPrimeCalcule(BigDecimal primeCalcule) {
        this.primeCalcule = primeCalcule;
    }

    public void setEmission(BigDecimal emission) {
        this.emission = emission;
    }

    public BigDecimal getPuissance() {
        return puissance;
    }

    public void setPuissance(BigDecimal puissance) {
        this.puissance = puissance;
    }

    public BigDecimal getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(BigDecimal prixTotal) {
        this.prixTotal = prixTotal;
    }

    public BigDecimal getPrimeTaxi() {
        return primeTaxi;
    }

    public void setPrimeTaxi(BigDecimal primeTaxi) {
        this.primeTaxi = primeTaxi;
    }

    public BigDecimal getMontantAide() {
        return montantAide;
    }

    public void setMontantAide(BigDecimal montantAide) {
        this.montantAide = montantAide;
    }

    public String getCommentCGD() {
        return commentCGD;
    }

    public void setCommentCGD(String commentCGD) {
        this.commentCGD = commentCGD;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

}
