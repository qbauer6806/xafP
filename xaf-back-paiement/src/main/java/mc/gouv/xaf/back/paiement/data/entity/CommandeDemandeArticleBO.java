package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
