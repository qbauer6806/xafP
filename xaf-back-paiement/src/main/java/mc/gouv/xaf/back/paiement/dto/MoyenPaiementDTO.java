package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class MoyenPaiementDTO {

    private String pkMoyensPaiements;

    private CommandeDTO commande;

    private LocalDateTime dateCreation;

    private LocalDateTime dateDerniereModification;

    private String moyenPaiementStatut;

    private String paymentMethodType;

    private String paymentMethodToken;

    private String effectiveBrand;

    private String expiryDate;

    private String paymentMethodAccount;

    private LocalDateTime cancellationDate;

    private String paymentMethodRecord;

    private String paymentMethodName;

    private String paymentSupplier;

}
