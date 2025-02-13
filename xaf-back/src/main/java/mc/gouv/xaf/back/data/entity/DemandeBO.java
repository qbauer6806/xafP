package mc.gouv.xaf.back.data.entity;

import com.fasterxml.jackson.databind.JsonNode;
import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

/**
 * Classe BO de la table DEM.DEMANDES
 * <br>
 * Attention ! À chaque ajout de Set<> dans ce BO, penser à mettre à jour l'algorithme de duplication de demandes pour
 * les prendre en compte. Et mettre à jour les transformers pour toute donnée ajoutée.
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES")
public class DemandeBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDES", nullable = false)
    private Integer pkDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_ACCESS")
    private AccessBO fkAccess;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

    @Column(name = "CONTENU", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode contenu;

    @Column(name = "CONTENU_TRAD", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode contenuTrad;

    @Column(name = "LANGUE", length = 2)
    @Size(max = 2)
    private String langue;

    @Column(name = "CANAL", length = 30, nullable = false)
    @Size(max = 30)
    private String canal;

    @Column(name = "OBSERVATIONS", length = 10000)
    @Size(max = 10000)
    private String observations;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dateCreation DESC")
    private Set<DemandesComplementsBO> demandesComplements;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesFilesBO> files;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesStatutsBO> statuts;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "FK_DEMANDESAGENTS")
    private DemandesAgentsBO agent;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_CONFIG")
    private DemandeConfigBO config;

    @Column(name = "CREE_PAR_AGENT_ID", length = 128)
    @Size(max = 128)
    private String creeParAgentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DERNIER_STATUT")
    private DemandesStatutsBO dernierStatut;

    @Column(name = "IDENTIFIANT", length = 30, nullable = false)
    @Size(min = 1, max = 30)
    private String identifiant;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesDataBO> data;

    @Column(name = "COURRIER_DATE_RECEPTION")
    private Date courrierDateReception;

    @Column(name = "COURRIER_REF_INTERNE", length = 256)
    @Size(max = 256)
    private String courrierRefInterne;

    @OneToMany(mappedBy = "fkDemandes", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesCourriersBO> courriers;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_DEMANDESUSAGERS")
    private DemandesUsagersBO usager;

    @Column(name = "RECAP_TYPE", length = 256)
    @Size(max = 256)
    private String recapType;

    @Column(name = "DONNEES_CERTIFIEES", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode donneesCertifiees;

    @Column(name = "MODIFICATION_TIMESTAMP")
    private Long modificationTimestamp;

    @Column(name = "CONTENU_INITIAL", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode contenuInitial;

    @Column(name = "META", columnDefinition = "TEXT")
    private String meta;

    @Column(name = "TYPE_CONNEXION_USAGER", length = 256)
    private String typeConnexionUsager;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector_contenu", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVectorContenu;

    // De type Integer et non DemandeBO (autrement dit : pas de foreign key en base)
    // Ceci afin d'être tranquille le jour où cette demande source doit être purgée (supprimée)
    @Column(name = "PK_DEMANDE_SOURCE")
    private Integer pkDemandeSource;

}
