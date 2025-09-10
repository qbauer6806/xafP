package mc.gouv.xaf.back.data.entity;

import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

/**
 * Classe BO de la table DEM.DEM_DEMANDES_AGENTS
 *
 * @author uek
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_AGENTS")
public class DemandesAgentsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_AGENT", nullable = false)
    private Integer pkAgent;

    @Column(name = "ID", length = 128)
    @Size(max = 128)
    private String id;

    @Column(name = "NOM", length = 128)
    @Size(max = 128)
    private String nom;

    @Column(name = "NOM_USAGE", length = 128)
    @Size(max = 128)
    private String nomUsage;

    @Column(name = "NOM_NAISSANCE", length = 128)
    @Size(max = 128)
    private String nomNaissance;

    @Column(name = "PRENOM", length = 128)
    @Size(max = 128)
    private String prenom;

    @Column(name = "MAIL", length = 128)
    @Size(max = 128)
    private String mail;

    @Column(name = "NOM_AFFICHAGE", length = 128)
    @Size(max = 128)
    private String nomAffichage;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

}
