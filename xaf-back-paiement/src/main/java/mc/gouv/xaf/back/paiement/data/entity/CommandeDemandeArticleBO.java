package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Classe BO de la table PMNT_COMMANDES_DEMANDES_ARTICLES
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
@Entity
@ToString
@Table(name = "PMNT_COMMANDES_DEMANDES_ARTICLES")
public class CommandeDemandeArticleBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDES_DEMANDES_ARTICLES", nullable = false)
    private Integer pkCommandesDemandesArticles;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_COMMANDES_DEMANDES")
    private CommandeDemandeBO commandeDemande;

    private String codeTarif;

    private double montant;
}
