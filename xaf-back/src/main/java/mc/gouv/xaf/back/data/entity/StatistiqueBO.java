package mc.gouv.xaf.back.data.entity;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * 
 * Classe BO de la table DEM.STATISTQUES
 *
 * @author qdeme
 *
 */
@Entity
@Table(name = "DEM_STATISTIQUES")
public class StatistiqueBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_STATISTIQUES", nullable = false)
    private Integer pkStatistiques;

    @Column(name = "DEMANDE_ID", nullable = false)
    private Integer demandeId;

    @Column(name = "STATUT_PUBLIC_LIBELLE", length = 64, nullable = false)
    @Size(min = 1, max = 64)
    private String statutPublicLibelle;

    @Column(name = "STATUT_INTERNE_LIBELLE", length = 64)
    @Size(min = 1, max = 64)
    private String statutInterneLibelle;

    @Column(name = "CANAL", length = 30, nullable = false)
    @Size(min = 0, max = 30)
    private String canal;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "DEMARCHE_ID", length = 128)
    @Size(min = 1, max = 128)
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
