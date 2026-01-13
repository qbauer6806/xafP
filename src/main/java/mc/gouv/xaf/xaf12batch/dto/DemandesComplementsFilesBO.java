package mc.gouv.xaf.xaf12batch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_COMPLEMENTS_FILES")
public class DemandesComplementsFilesBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMPLEMENTSFILES", nullable = false)
    private Integer pkDemandesComplementsFiles;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "CONTENU", length = 100000)
    private String contenu;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDESCOMPLEMENTS")
    private DemandesComplementsBO fkDemandesComplements;

    @Column(name = "SUPPRIMEE")
    private boolean supprimee;

}
