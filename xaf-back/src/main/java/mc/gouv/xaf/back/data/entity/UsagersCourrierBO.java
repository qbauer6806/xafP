package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * Classe BO de la table DEM.USAGERS_COURRIER
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_USAGERS_COURRIER")
public class UsagersCourrierBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_USAGERSCOURRIER", nullable = false)
    private Integer pkUsagersCourrier;

    @Column(name = "LOGIN", length = 20, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 20)
    private String login;

    @Column(name = "TITRE")
    private Integer titre;

    @Column(name = "NOM", length = 50)
    @Size(max = 50)
    private String nom;

    @Column(name = "PRENOM", length = 20)
    @Size(max = 20)
    private String prenom;

    @Column(name = "RAISON_SOCIALE", length = 100)
    @Size(max = 100)
    private String raisonSociale;

    @Column(name = "ADRESSE1", length = 128, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 128)
    private String adresse1;

    @Column(name = "ADRESSE2", length = 128)
    @Size(max = 128)
    private String adresse2;

    @Column(name = "ADRESSE_COMPLEMENT", length = 128)
    @Size(max = 128)
    private String adresseComplement;

    @Column(name = "CODE_POSTAL", length = 10, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 10)
    private String codePostal;

    @Column(name = "VILLE", length = 50, nullable = false)
    @NotEmpty
    @Size(min = 1, max = 50)
    private String ville;

    @Column(name = "PAYS", length = 2, nullable = false)
    @NotEmpty
    @Size(min = 2, max = 2)
    private String pays;

    @Column(name = "TELEPHONE", length = 64)
    @Size(max = 64)
    private String telephone;

    @Column(name = "EMAIL", length = 256)
    @Size(max = 256)
    private String email;

    @Column(name = "DATE_CREATION", nullable = false)
    private Date dateCreation;

    @Column(name = "DATE_DERMODIF", nullable = false)
    private Date dateDerModif;

}
