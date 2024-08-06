package mc.gouv.xaf.back.paiement.data.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@ToString
@Table(name = "PMNT_COMMANDES_OPERATIONS")
public class CommandeOperationBO {

    @Id
    @Column(name = "PK_OPERATIONS", nullable = false)
    private String pkOperations;

    @Enumerated(EnumType.STRING)
    private OperationTypeEnum operationType;

    @Enumerated(EnumType.STRING)
    private OperationStatutEnum operationStatut;

    @ManyToOne
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    private Double montant;

    private String numeroAutorisation;

    private String numeroFacture;

    private String codeRetour;

    private String libelle;

}
