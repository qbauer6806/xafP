package mc.gouv.xaf.back.paiement.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.data.entity.DemandeBO;

import jakarta.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@ToString
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

}
