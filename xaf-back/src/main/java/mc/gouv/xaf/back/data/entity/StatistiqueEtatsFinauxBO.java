package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/**
 * 
 * Classe BO de la table DEM.STATISTQUES filtrée sur des statuts finaux Permet de faire une jointure avec la même table
 * sur le statut SUPPRIMEE (cad purgee) Voir utilisation dans PurgeDemandesServiceImpl
 *
 * Pour injecter les parametres du filter, il faut ajouter la valeur dans la session hibernate comme suit: Session
 * session = em.unwrap(Session.class); session.enableFilter("filtreStatuts").setParameterList("statuts",
 * demarchesDataProvider.getStatutsAPurger()); List<Object> statsDemandesPurgees =
 * statRepository.findAllBetweenDates(dateDebutOffset, new Date());
 * 
 * @author agaidi.ext
 *
 */
@Entity
@Table(name = "DEM_STATISTIQUES")
@FilterDef(name = "filtreStatuts", defaultCondition = "statut_public in (:statuts)", parameters = @ParamDef(name = "statuts", type = "string"))
@Filter(name = "filtreStatuts", condition = "statut_public in (:statuts)")
public class StatistiqueEtatsFinauxBO {

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


    private String dernierStatut;

    private Date dernierStatutDate;

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

    public String getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(String dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public Date getDernierStatutDate() {
        return dernierStatutDate;
    }

    public void setDernierStatutDate(Date dernierStatutDate) {
        this.dernierStatutDate = dernierStatutDate;
    }
}
