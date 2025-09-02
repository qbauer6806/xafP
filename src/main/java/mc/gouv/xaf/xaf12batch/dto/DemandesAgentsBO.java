package mc.gouv.xaf.xaf12batch.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "DEM_DEMANDES_AGENTS")
public class DemandesAgentsBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_AGENT", nullable = false)
    private Integer pkAgent;

    @Column(name = "ID", length = 128)
    @Size(max = 128)
    private String id;

    @Column(name = "NOM", length = 128)
    @Size(max = 128)
    private String nom;

    @Column(name = "NOM_USAGE", length = 128)
    @Size(max = 128)
    private String nomUsage;

    @Column(name = "NOM_NAISSANCE", length = 128)
    @Size(max = 128)
    private String nomNaissance;

    @Column(name = "PRENOM", length = 128)
    @Size(max = 128)
    private String prenom;

    @Column(name = "MAIL", length = 128)
    @Size(max = 128)
    private String mail;

    @Column(name = "NOM_AFFICHAGE", length = 128)
    @Size(max = 128)
    private String nomAffichage;


}
