package mc.gouv.xaf.servlet.dto;

public class CustomRequestRechercheDataDTO {

    private String numeroFacture;
    private String numeroContrat;
    private String numeroTiers;

    public String getNumeroFacture() {
        return numeroFacture;
    }

    public void setNumeroFacture(String numeroFacture) {
        this.numeroFacture = numeroFacture;
    }

    public String getNumeroContrat() {
        return numeroContrat;
    }

    public void setNumeroContrat(String numeroContrat) {
        this.numeroContrat = numeroContrat;
    }

    public String getNumeroTiers() {
        return numeroTiers;
    }

    public void setNumeroTiers(String numeroTiers) {
        this.numeroTiers = numeroTiers;
    }

}
