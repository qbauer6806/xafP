package mc.gouv.xaf.back.paiement.data.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementStatutEnum;
import mc.gouv.xaf.back.paiement.data.enums.MoyenPaiementTypeEnum;
import mc.gouv.xaf.shared.paiement.enums.PSPEnum;

@Setter
@Getter
@Entity
@ToString
@Table(name = "PMNT_MOYENS_PAIEMENTS")
public class MoyenPaiementBO {

    @Id
    @Column(name = "PK_MOYENS_PAIEMENTS", nullable = false)
    private String pkMoyensPaiements;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COMMANDES")
    private CommandeBO commande;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    @Enumerated(EnumType.STRING)
    private MoyenPaiementStatutEnum moyenPaiementStatut;

    private String paymentMethodType;

    private String paymentMethodToken;

    private String effectiveBrand;

    private LocalDateTime cancellationDate;

    private String paymentMethodRecord;

    private String paymentMethodName;

    @Enumerated(EnumType.STRING)
    private PSPEnum paymentSupplier;

    @Override
    public String toString() {
        return "MoyenPaiementBO{" +
                "pkMoyenPaiement='" + pkMoyensPaiements + '\'' +
                ", commande=" + commande +
                ", dateCreation='" + dateCreation + '\'' +
                ", dateDerniereModification='" + dateDerniereModification + '\'' +
                ", paymentMethodType=" + paymentMethodType +
                ", paymentMethodToken=" + paymentMethodToken +
                ", cancellationDate=" + cancellationDate +
                ", paymentMethodRecord='" + paymentMethodRecord + '\'' +
                ", paymentMethodName='" + paymentMethodName + '\'' +
                '}';
    }
}
