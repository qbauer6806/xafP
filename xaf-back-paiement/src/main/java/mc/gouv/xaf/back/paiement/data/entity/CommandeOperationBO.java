package mc.gouv.xaf.back.paiement.data.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.data.enums.OperationStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.OperationTypeEnum;

@Entity
@Table(name = "PMNT_COMMANDES_OPERATIONS")
@Getter
@Setter
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

    private String transactionReference;

    private String errorCode;

    private String errorMessage;

    private Double montant;

    @Override
    public String toString() {
        return "CommandeOperationBO{" +
                "pkOperation='" + pkOperations + '\'' +
                ", operationType=" + operationType +
                ", operationStatut=" + operationStatut +
                ", dateCreation=" + dateCreation +
                ", errorCode=" + errorCode +
                ", errorMessage=" + errorMessage +
                ", montant=" + montant +
                ", transactionReference='" + transactionReference + '\'' +
                '}';
    }

}
