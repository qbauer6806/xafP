package mc.gouv.xaf.shared.dto;

import java.util.Date;

public class PurgeDemandeDTO {

    private String identifiantDemande;

    private String statutFinal;

    private Date dateStatutFinal;

    private Date dateSuppression;

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
    }

    public String getStatutFinal() {
        return statutFinal;
    }

    public void setStatutFinal(String statutFinal) {
        this.statutFinal = statutFinal;
    }

    public Date getDateStatutFinal() {
        return dateStatutFinal;
    }

    public void setDateStatutFinal(Date dateStatutFinal) {
        this.dateStatutFinal = dateStatutFinal;
    }

    public Date getDateSuppression() {
        return dateSuppression;
    }

    public void setDateSuppression(Date dateSuppression) {
        this.dateSuppression = dateSuppression;
    }
}
