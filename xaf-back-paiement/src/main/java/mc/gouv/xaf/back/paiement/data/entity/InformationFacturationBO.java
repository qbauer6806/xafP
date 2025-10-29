package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Entity
@Table(name = "PMNT_INFORMATIONS_FACTURATIONS")
@Getter
@Setter
public class InformationFacturationBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_INFORMATIONS_FACTURATION", nullable = false)
    private Integer pkInformationsFacturation;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    private LocalDateTime dateCreation;

    private Short civilite;

    private String prenom;

    private String nom;

    private String raisonSociale;

    @Column(name = "ADRESSE_LIGNE1")
    private String adresseLigne1;

    @Column(name = "ADRESSE_LIGNE2", nullable = true)
    private String adresseLigne2;

    @Column(name = "ADRESSE_LIGNE3", nullable = true)
    private String adresseLigne3;

    private String langue;

    private String codePostal;

    private String ville;

    private String pays;

    private String email;

    @Override
    public String toString() {
        return "InformationsFacturationBO{" +
                "pkInformationsFacturation='" + pkInformationsFacturation + '\'' +
                ", commande=" + commande +
                ", dateCreation='" + dateCreation + '\'' +
                ", civilite='" + civilite + '\'' +
                ", prenom=" + prenom +
                ", nom=" + nom +
                ", raisonSociale=" + raisonSociale +
                ", adresseLigne1='" + adresseLigne1 + '\'' +
                ", adresseLigne2='" + adresseLigne2 + '\'' +
                ", adresseLigne3='" + adresseLigne3 + '\'' +
                ", langue='" + langue + '\'' +
                ", codePostal='" + codePostal + '\'' +
                ", ville='" + ville + '\'' +
                ", pays='" + pays + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

}
