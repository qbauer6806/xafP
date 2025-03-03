package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOutputDTO {

    public RegisterOutputDTO(String formToken) {
        this.formToken = formToken;
    }
    private String formToken;

}
