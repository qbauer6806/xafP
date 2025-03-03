package mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InfoOutputDTO {

    private String reference;

    private PaymentMethodInformationDTO paymentMethodInformation;

}
