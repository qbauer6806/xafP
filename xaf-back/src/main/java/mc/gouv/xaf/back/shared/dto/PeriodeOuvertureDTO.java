package mc.gouv.xaf.back.shared.dto;

import java.util.Date;

/**
 * Représente une période d'ouverture d'une démarche
 * 
 * @author qdeme
 *
 */
public class PeriodeOuvertureDTO {

    private Integer pkPeriodesOuverture;

    private String demarcheId;
    
    private Date dateDebut;
    
    private Date dateFin;

    public Integer getPkPeriodesOuverture() {
        return pkPeriodesOuverture;
    }

    public void setPkPeriodesOuverture(Integer pkPeriodesOuverture) {
        this.pkPeriodesOuverture = pkPeriodesOuverture;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }
    
}
