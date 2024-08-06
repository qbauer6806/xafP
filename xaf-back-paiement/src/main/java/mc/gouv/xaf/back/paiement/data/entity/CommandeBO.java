package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Entity
@Table(name = "PMNT_COMMANDES")
@ToString
public class CommandeBO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PK_COMMANDES", nullable = false)
    private Integer pkCommandes;

    private LocalDateTime dateCreation;

    private double montantInitial;

    private double montantDejaCapture;

    private double montantRestant;

    @OneToOne(mappedBy = "commande", cascade = CascadeType.ALL)
    private MoyenPaiementBO moyenPaiement;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeDemandeBO> commandesDemandes;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CommandeOperationBO> operations;

}
