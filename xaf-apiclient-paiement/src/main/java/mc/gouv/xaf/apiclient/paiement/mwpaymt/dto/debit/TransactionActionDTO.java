package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.enums.ActionDebitEnum;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionActionDTO {

    @NotNull
    private ActionDebitEnum actionDebit;

    @NotNull
    private LocalDateTime dateCreationDebit;

    @NotNull
    private LocalDateTime dateDebit;
}
