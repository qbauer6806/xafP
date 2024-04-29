package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;

import java.util.Date;

/**
 * Modélise d'un statistique
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatistiqueDTO {

    private Integer pkStatistiques;

    private Integer demandeId;

    private String statutPublic;

    private String canal;

    private Date date;

    private String demarcheId;

    private String identifiantDemande;

    private TypeConnexionUsagerEnum typeConnexionUsager;

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

    public String getStatutPublic() {
        return statutPublic;
    }

    public void setStatutPublic(String statutPublicLibelle) {
        this.statutPublic = statutPublicLibelle;
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

    public String getIdentifiantDemande() {
        return identifiantDemande;
    }

    public void setIdentifiantDemande(String identifiantDemande) {
        this.identifiantDemande = identifiantDemande;
    }

    public TypeConnexionUsagerEnum getTypeConnexionUsager() {
        return typeConnexionUsager;
    }

    public void setTypeConnexionUsager(TypeConnexionUsagerEnum typeConnexionUsager) {
        this.typeConnexionUsager = typeConnexionUsager;
    }
}
