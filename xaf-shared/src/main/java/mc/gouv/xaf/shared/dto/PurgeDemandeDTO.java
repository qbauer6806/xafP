package mc.gouv.xaf.shared.dto;

import java.util.Date;

public class PurgeDemandeDTO {

    private String identifiantDemande;

    private Date dateStatutFinal;

    private Date dateSuppression;

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
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
