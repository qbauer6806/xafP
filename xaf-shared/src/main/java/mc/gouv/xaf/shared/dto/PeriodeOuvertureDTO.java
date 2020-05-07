package mc.gouv.xaf.shared.dto;

import java.time.LocalDate;

/**
 * Représente une période d'ouverture d'une démarche
 *
 * @author qdeme
 */
public class PeriodeOuvertureDTO {

    private Integer pkPeriodesOuverture;

    private String demarcheId;

    private LocalDate dateDebut;

    private LocalDate dateFin;

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

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDate getDateFin() {
        return dateFin;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

}
