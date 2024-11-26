package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;

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
