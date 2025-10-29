package mc.gouv.xaf.back.paiement.data.entity;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.data.entity.DemandeBO;

@Setter
@Getter
@Entity
@Table(name = "PMNT_COMMANDES_DEMANDES")
public class CommandeDemandeBO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDES_DEMANDES", nullable = false)
    private Integer pkCommandesDemandes;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_DEMANDES")
    private DemandeBO demande;

    private double montant;

    @OneToMany(mappedBy = "commandeDemande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeDemandeArticleBO> commandesDemandesArticles;


    @Override
    public String toString() {
        return "CommandeDemandeBO{" +
                "pkComandeDemande=" + pkCommandesDemandes +
                ", commande=" + commande +
                ", demande=" + demande +
                ", montant=" + montant +
                '}';
    }
}
