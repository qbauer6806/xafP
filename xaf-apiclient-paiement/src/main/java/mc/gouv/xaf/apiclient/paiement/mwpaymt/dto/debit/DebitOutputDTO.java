package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitOutputDTO {

    @NotNull
    private TransactionActionDTO transactionAction;

}
