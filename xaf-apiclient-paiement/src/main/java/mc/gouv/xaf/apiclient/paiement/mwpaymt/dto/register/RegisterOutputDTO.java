package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOutputDTO {

    public RegisterOutputDTO(String formToken) {
        this.formToken = formToken;
    }
    private String formToken;
    private String orderId;
    private String callbackUri;

}
