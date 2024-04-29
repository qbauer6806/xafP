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

    @Column(name = "STATUT_PUBLIC", length = 64, nullable = false)
    @Size(min = 1, max = 64)
    private String statutPublic;

    @Column(name = "CANAL", length = 30, nullable = false)
    @Size(min = 0, max = 30)
    private String canal;

    @Column(name = "DATE", nullable = false)
    private Date date;

    @Column(name = "DEMARCHE_ID", length = 128)
    @Size(min = 1, max = 128)
    private String demarcheId;

    @Column(name = "IDENTIFIANT_DEMANDE", length = 30)
    @Size(min = 1, max = 30)
    private String identifiantDemande;

    @Column(name = "TYPE_CONNEXION_USAGER", length = 256, nullable = true)
    private String typeConnexionUsager;

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

    public void setStatutPublic(String statutPublic) {
        this.statutPublic = statutPublic;
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

    public String getTypeConnexionUsager() {
        return typeConnexionUsager;
    }

    public void setTypeConnexionUsager(String typeConnexionUsager) {
        this.typeConnexionUsager = typeConnexionUsager;
    }
}
