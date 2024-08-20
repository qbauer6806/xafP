package mc.gouv.xaf.xaf12batch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_USAGERS")
public class DemandesUsagersBO {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "ETAT", length = 128)
    @Size(max = 128)
    private String etat;

    @Column(name = "EMAIL", length = 128)
    @Size(max = 128)
    private String email;

    @Column(name = "TITRE", length = 128)
    @Size(max = 128)
    private String titre;

    @Column(name = "PRENOM", length = 128)
    @Size(max = 128)
    private String prenom;

    @Column(name = "NOM", length = 128)
    @Size(max = 128)
    private String nom;

    @Column(name = "RAISON_SOCIALE", length = 128)
    @Size(max = 128)
    private String raisonSociale;

    @Column(name = "ADRESSE_1", length = 128)
    @Size(max = 128)
    private String adresse1;

    @Column(name = "ADRESSE_2", length = 128)
    @Size(max = 128)
    private String adresse2;

    @Column(name = "COMPLEMENT_ADRESSE", length = 256)
    @Size(max = 256)
    private String complementAdresse;

    @Column(name = "CODE_POSTAL", length = 256)
    @Size(max = 256)
    private String codePostal;

    @Column(name = "VILLE", length = 256)
    @Size(max = 256)
    private String ville;

    @Column(name = "NOM_PAYS", length = 128)
    @Size(max = 128)
    private String nomPays;

    @Column(name = "LOGIN", length = 128)
    @Size(max = 128)
    private String login;

}
