package mc.gouv.xaf.back.data.entity;

import java.util.Date;

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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.BROUILLONS_FILES
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_BROUILLONS_FILES")
public class BrouillonsFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_BROUILLONSFILES", nullable = false)
    private Integer pkBrouillonsFiles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_BROUILLONS")
    private BrouillonBO fkBrouillons;

    @Column(name = "NAME", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String name;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "meta", length = 512)
    @Size(min = 0, max = 512)
    private String meta;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "TYPEDOC", length = 128)
    private String typedoc;

}
