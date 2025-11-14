package mc.gouv.xaf.shared.paiement.infopaiement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AnswerDTO {
    public AnswerDTO(String token) {
        this.formToken = token;
    }
    private String formToken;

}
