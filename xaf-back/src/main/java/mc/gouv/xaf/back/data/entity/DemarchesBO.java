package mc.gouv.xaf.back.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.DEMARCHES
 *
 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_DEMARCHES")
public class DemarchesBO {

    @Id
    @NotBlank
    @Column(name = "PK_DEMARCHEID", nullable = false)
    @Size(min = 1, max = 128)
    private String pkDemarches;

    @Column(name = "NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nom;

    @Column(name = "NOM_EN", length = 256)
    @Size(max = 256)
    private String nomEn;

    @Column(name = "EMAIL_SERVICE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailService;

    @Column(name = "EMAIL_REPLYTO", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailReplyto;

    @Column(name = "EMAIL_REPLYTO_NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailReplytoNom;

    @Column(name = "EMAIL_FROM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailFrom;

    @Column(name = "EMAIL_FROM_NOM", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 256)
    private String emailFromNom;

    @Column(name = "NOM_DIRECTION", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nomDirection;

    @Column(name = "NOM_SOUS_DIRECTION", length = 256)
    @Size(max = 256)
    private String nomSousDirection;

    @Column(name = "NOM_FOOTER", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String nomFooter;

    @Column(name = "ADRESSE_SERVICE", length = 256)
    @NotBlank
    @Size(min = 1, max = 256)
    private String adresseService;

    @Column(name = "IDENTIFIANT_PREFIXE", length = 256, nullable = false)
    @NotBlank
    @Size(min = 1, max = 128)
    private String identifiantPrefixe;

    @Column(name = "LANGUES", length = 256)
    @Size(max = 256)
    private String langues;

    @Column(name = "NOM_SOUS_DIRECTION_COMPLEMENT", length = 256)
    @Size(max = 256)
    private String nomSousDirectionComplement;
    @Column(name = "TELEPHONE_SERVICE", length = 256)
    @Size(max = 256)
    private String telephoneService;

    @Column(name = "NOM_DIRECTION_EN", length = 256)
    @Size(max = 256)
    private String nomDirectionEn;

    @Column(name = "NOM_SOUS_DIRECTION_EN", length = 256)
    @Size(max = 256)
    private String nomSousDirectionEn;

    @Column(name = "NOM_SOUS_DIRECTION_COMPLEMENT_EN", length = 256)
    @Size(max = 256)
    private String nomSousDirectionComplementEn;

}
