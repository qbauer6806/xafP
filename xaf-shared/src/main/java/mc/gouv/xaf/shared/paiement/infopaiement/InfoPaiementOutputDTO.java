package mc.gouv.xaf.shared.paiement.infopaiement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InfoPaiementOutputDTO {
    private String status;
    private String reference;
    private AnswerDTO answer;

}
