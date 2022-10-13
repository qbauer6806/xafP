package mc.gouv.xaf.back.paiement.data.entity;

import javax.persistence.*;

/**
 * Classe BO de la table PMNT_COMMANDES_DEMANDES_ARTICLES
 *
 * @author mboutelier.ext
 */
@Entity
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

    public Integer getPkCommandesDemandesArticles() {
        return pkCommandesDemandesArticles;
    }

    public void setPkCommandesDemandesArticles(Integer pkCommandesDemandesArticles) {
        this.pkCommandesDemandesArticles = pkCommandesDemandesArticles;
    }

    public CommandeDemandeBO getCommandeDemande() {
        return commandeDemande;
    }

    public void setCommandeDemande(CommandeDemandeBO commandeDemande) {
        this.commandeDemande = commandeDemande;
    }

    public String getCodeTarif() {
        return codeTarif;
    }

    public void setCodeTarif(String codeTarif) {
        this.codeTarif = codeTarif;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    @Override
    public String toString() {
        return "CommandeDemandeArticleBO{" +
                "pkCommandesDemandesArticles=" + pkCommandesDemandesArticles +
                ", commandeDemande=" + commandeDemande +
                ", codeTarif='" + codeTarif + '\'' +
                ", montant=" + montant +
                '}';
    }
}
