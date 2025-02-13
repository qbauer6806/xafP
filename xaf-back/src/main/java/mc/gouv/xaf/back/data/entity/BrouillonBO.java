package mc.gouv.xaf.back.data.entity;

import com.fasterxml.jackson.databind.JsonNode;
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
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Date;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Classe BO de la table DEM.BROUILLONS
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_BROUILLONS")
public class BrouillonBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_BROUILLONS", nullable = false)
    private Integer pkBrouillons;

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

    @OneToMany(mappedBy = "fkBrouillons", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<BrouillonsFilesBO> files;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "FK_CONFIG")
    private DemandeConfigBO config;

    @Column(name = "RECAP_TYPE", length = 256)
    @Size(max = 256)
    private String recapType;

    @Column(name = "META", columnDefinition = "TEXT")
    private String meta;

    @Column(name = "CONTENU_INITIAL", columnDefinition = "JSONB")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode contenuInitial;

}
