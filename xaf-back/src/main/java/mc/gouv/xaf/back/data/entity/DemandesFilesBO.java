package mc.gouv.xaf.back.data.entity;

import io.hypersistence.utils.hibernate.type.search.PostgreSQLTSVectorType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import jakarta.validation.constraints.NotBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.Date;

/**
 * 
 * Classe BO de la table DEM.DEMANDES_FILES
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_FILES")
public class DemandesFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESFILES", nullable = false)
    private Integer pkDemandesFiles;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "NAME", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String name;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "meta", length = 512)
    @Size(max = 512)
    private String meta;
    
    @Column(name = "DATE")
    private Date date;

    @Column(name = "TYPEDOC", length = 128)
    private String typedoc;

    // Correspond à la checkbox de vérification de pièces jointes dans le BO
    @Column(name = "VERIFICATION")
    private boolean verification;

    @Column(name = "CONTENU", length = 100000)
    private String contenu;

    @Type(PostgreSQLTSVectorType.class)
    @Column(name = "search_vector",columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

}
