package mc.gouv.xaf.back.data.entity;

import lombok.Getter;
import lombok.Setter;
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
 * Classe BO de la table DEM.DEMANDES_COURRIERS
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_COURRIERS")
public class DemandesCourriersBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOURRIERS", nullable = false)
    private Integer pkDemandesCourriers;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDESSTATUTS")
    private DemandesStatutsBO fkDemandesStatuts;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "NAME", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String name;

    @Column(name = "URL", length = 1024, nullable = false)
    @NotBlank
    @Size(min = 1, max = 1024)
    private String url;

    @Column(name = "META", length = 512)
    @Size(max = 512)
    private String meta;

    @Column(name = "DATE_PRINTED")
    private Date datePrinted;

    @Column(name = "IDENTIFIANT", length = 128)
    @Size(max = 128)
    private String identifiant;

    @Column(name = "search_vector", columnDefinition = "tsvector", insertable = false, updatable = false)
    private String searchVector;

}
