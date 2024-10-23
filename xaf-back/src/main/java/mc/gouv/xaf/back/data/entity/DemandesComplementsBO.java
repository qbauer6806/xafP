package mc.gouv.xaf.back.data.entity;

import java.util.Date;
import java.util.Set;

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

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.DEMANDES_COMPLEMENTS
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_COMPLEMENTS")
public class DemandesComplementsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_DEMANDESCOMPLEMENTS", nullable = false)
    private Integer pkDemandesComplements;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO fkDemandes;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_REPONSE")
    private Date dateReponse;

    @Column(name = "CODE_MOTIF", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String codeMotif;

    @Column(name = "QUESTION", length = 8000)
    @Size(max = 8000)
    private String question;

    @Column(name = "REPONSE", length = 8000)
    @Size(max = 8000)
    private String reponse;

    @Column(name = "STATUT", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String statut;

    @Column(name = "AGENT_ID", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String agentId;

    @Column(name = "REPONSE_AGENT_ID", length = 128)
    @Size(max = 128)
    private String reponseAgentId;

    @Column(name = "REPONSE_USAGER_ID")
    private Integer reponseUsagerId;

    @OneToMany(mappedBy = "fkDemandesComplements", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<DemandesComplementsFilesBO> files;

}
