package mc.gouv.xaf.back.paiement.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.paiement.enums.StatutDebitEnum;
import java.time.LocalDateTime;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DebitDTO {

    @JsonInclude()
    private StatutDebitEnum statut;
    @JsonInclude()
    private LocalDateTime expectedCaptureDate;
}
