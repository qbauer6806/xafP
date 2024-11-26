package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.STATISTQUES
 *
 * @author qdeme
 */
@Setter
@Getter
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

    @Column(name = "TYPE_CONNEXION_USAGER", length = 256)
    private String typeConnexionUsager;

}
