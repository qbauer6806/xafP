package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
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
@Setter
@Getter
@Entity
@Table(name = "DEM_STATISTIQUES")
@FilterDef(name = "filtreStatuts", defaultCondition = "statut_public in (:statuts)", parameters = @ParamDef(name = "statuts", type = String.class))
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
    @Size(max = 30)
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

}
