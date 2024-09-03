package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe BO de la table DEM.MOTIFS
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_MOTIFS")
public class MotifBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_MOTIFS", nullable = false)
    private Integer pkMotifs;

    @Column(name = "CODE", length = 128, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String code;

    @Column(name = "LIBELLE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String libelle;

    @Column(name = "STATUT", length = 64, nullable = false)
    @NotBlank
    @Size(min = 1, max = 64)
    private String statut;

    @Column(name = "LANGUE", length = 2, nullable = false)
    @NotBlank
    @Size(min = 1, max = 2)
    private String langue;

    @Column(name = "DATE_ARCHIVE")
    private Date dateArchive;
    
    @Column(name = "COMMENTAIRE_PREREMPLI", length = 2048)
    @Size(max = 2048)
    private String commentairePrerempli;

    @Column(name = "TEXTE_A_ENVOYER", length = 2048)
    @Size(max = 2048)
    private String texteAEnvoyer;

}
