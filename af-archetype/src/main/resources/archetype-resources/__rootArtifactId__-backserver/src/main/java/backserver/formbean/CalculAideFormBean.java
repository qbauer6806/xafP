#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.backserver.formbean;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;

/**
 * Formulaire pour le partie de Calcul d'aide sur la page de traitement.
 * 
 * @author dsaidiparto.ext
 *
 */
public class CalculAideFormBean {

    @NotNull
    private String typeUsager;
    @NotNull
    private String prixBasVehicule = "0,00";
    private String remiseDeduire = "0,00";
    private String montantBatterie = "0,00";
    private String tva = "0,00";
    @NotNull
    private String prixTotalVehicule = "0,00";
    @NotNull
    private String applicationPourcentage = "0,00";
    @NotNull
    private String montantSimule = "0,00";
    @NotNull
    private String montantSimulePlus20 = "0,00";
    @NotNull
    private String montantSimuleMoins20 = "0,00";
    @NotNull
    private String primeForfaitaire = "0,00";
    @NotNull
    private String primeCalcule = "0,00";
    private String emission;
    private String puissance;
    private String prixTotal;
    @NotNull
    private String primeTaxi = "0,00";
    @NotNull
    private String montantAide = "0,00";
    private String commentCGD;

    public String getTypeUsager() {
        return typeUsager;
    }

    public void setTypeUsager(String typeUsager) {
        this.typeUsager = typeUsager;
    }

    public String getPrixBasVehicule() {
        return prixBasVehicule;
    }

    public void setPrixBasVehicule(String prixBasVehicule) {
        if (!StringUtils.isBlank(prixBasVehicule)) {
            this.prixBasVehicule = prixBasVehicule;
        }
    }

    public String getRemiseDeduire() {
        return remiseDeduire;
    }

    public void setRemiseDeduire(String remiseDeduire) {
        if (!StringUtils.isBlank(remiseDeduire)) {
            this.remiseDeduire = remiseDeduire;
        }
    }

    public String getMontantBatterie() {
        return montantBatterie;
    }

    public void setMontantBatterie(String montantBatterie) {
        if (!StringUtils.isBlank(montantBatterie)) {
            this.montantBatterie = montantBatterie;
        }
    }

    public String getTva() {
        return tva;
    }

    public void setTva(String tva) {
        if (!StringUtils.isBlank(tva)) {
            this.tva = tva;
        }
    }

    public String getPrixTotalVehicule() {
        return prixTotalVehicule;
    }

    public void setPrixTotalVehicule(String prixTotalVehicule) {
        if (!StringUtils.isBlank(prixTotalVehicule)) {
            this.prixTotalVehicule = prixTotalVehicule;
        }
    }

    public String getApplicationPourcentage() {
        return applicationPourcentage;
    }

    public void setApplicationPourcentage(String applicationPourcentage) {
        if (!StringUtils.isBlank(applicationPourcentage)) {
            this.applicationPourcentage = applicationPourcentage;
        }
    }

    public String getMontantSimule() {

        return montantSimule;
    }

    public String getMontantSimulePlus20() {
        return montantSimulePlus20;
    }

    public void setMontantSimulePlus20(String montantSimulePlus20) {
        if (!StringUtils.isBlank(montantSimulePlus20)) {
            this.montantSimulePlus20 = montantSimulePlus20;
        }
    }

    public String getMontantSimuleMoins20() {
        return montantSimuleMoins20;
    }

    public void setMontantSimuleMoins20(String montantSimuleMoins20) {
        if (!StringUtils.isBlank(montantSimuleMoins20)) {
            this.montantSimuleMoins20 = montantSimuleMoins20;
        }
    }

    public void setMontantSimule(String montantSimule) {
        if (!StringUtils.isBlank(montantSimule)) {
            this.montantSimule = montantSimule;
        }
    }

    public String getPrimeForfaitaire() {
        return primeForfaitaire;
    }

    public void setPrimeForfaitaire(String primeForfaitaire) {
        if (!StringUtils.isBlank(primeForfaitaire)) {
            this.primeForfaitaire = primeForfaitaire;
        }
    }

    public String getPrimeCalcule() {
        return primeCalcule;
    }

    public void setPrimeCalcule(String primeCalcule) {
        if (!StringUtils.isBlank(primeCalcule)) {
            this.primeCalcule = primeCalcule;
        }
    }

    public String getEmission() {
        return emission;
    }

    public void setEmission(String emission) {
        this.emission = emission;
    }

    public String getPuissance() {
        return puissance;
    }

    public void setPuissance(String puissance) {
        this.puissance = puissance;
    }

    public String getPrixTotal() {
        return prixTotal;
    }

    public void setPrixTotal(String prixTotal) {
        this.prixTotal = prixTotal;
    }

    public String getPrimeTaxi() {
        return primeTaxi;
    }

    public void setPrimeTaxi(String primeTaxi) {
        if (!StringUtils.isBlank(primeTaxi)) {
            this.primeTaxi = primeTaxi;
        }
    }

    public String getMontantAide() {
        return montantAide;
    }

    public void setMontantAide(String montantAide) {
        this.montantAide = montantAide;
    }

    public String getCommentCGD() {
        return commentCGD;
    }

    public void setCommentCGD(String commentCGD) {
        this.commentCGD = commentCGD;
    }

}
