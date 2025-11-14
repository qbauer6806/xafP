package mc.gouv.xaf.shared.paiement.mongichet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
public class PaymentMethodReferenceDTO {

    UUID id;

    UUID profileId;

    //ex: CARD
    String paymentMethodType;

    // The payment method's alias
    String paymentMethodToken;

    // ex: My new card
    String paymentMethodName;

    //  ex: LYRA_COLLECT
    String paymentSupplier;

    // ex: RESCART
    String tokenSupplier;

    Boolean isMarkedForDeletion;

    LocalDateTime createdAt;
}
