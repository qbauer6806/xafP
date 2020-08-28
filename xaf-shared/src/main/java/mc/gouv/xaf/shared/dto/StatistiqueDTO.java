package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

/**
 * Modélise d'un statistique
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatistiqueDTO {

    private Integer pkStatistiques;

    private Integer demandeId;

    private String statutPublicLibelle;

    private String statutInterneLibelle;

    private String canal;

    private Date date;

    private String demarcheId;

    public Integer getPkStatistiques() {
        return pkStatistiques;
    }

    public void setPkStatistiques(Integer pkStatistiques) {
        this.pkStatistiques = pkStatistiques;
    }

    public Integer getDemandeId() {
        return demandeId;
    }

    public void setDemandeId(Integer demandeId) {
        this.demandeId = demandeId;
    }

    public String getStatutPublicLibelle() {
        return statutPublicLibelle;
    }

    public void setStatutPublicLibelle(String statutPublicLibelle) {
        this.statutPublicLibelle = statutPublicLibelle;
    }

    public String getStatutInterneLibelle() {
        return statutInterneLibelle;
    }

    public void setStatutInterneLibelle(String statutInterneLibelle) {
        this.statutInterneLibelle = statutInterneLibelle;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDemarcheId() {
        return demarcheId;
    }

    public void setDemarcheId(String demarcheId) {
        this.demarcheId = demarcheId;
    }
}
