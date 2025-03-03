package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.common;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TransactionInformationDTO {

    private String orderId;
    private String currency;
    private String metadatakey;
    private String metadatavalue;
    private float amount;
}
