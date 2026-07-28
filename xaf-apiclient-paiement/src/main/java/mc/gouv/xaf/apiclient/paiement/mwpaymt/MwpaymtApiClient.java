package mc.gouv.xaf.apiclient.paiement.mwpaymt;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.constants.MwpaymtConstant;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.debit.DebitOutputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.InfoCancelInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.info.PaymentMethodInformationDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterInputDTO;
import mc.gouv.xaf.apiclient.paiement.mwpaymt.dto.register.RegisterOutputDTO;
import org.springframework.http.MediaType;

public class MwpaymtApiClient extends AfApiClient {

    public MwpaymtApiClient(String serviceUrl, String bearerToken) {
        super(serviceUrl, bearerToken);
    }

    public RegisterOutputDTO getToken(RegisterInputDTO input) {
        return getRestClient().post().uri("/" + MwpaymtConstant.REGISTER_PATH).contentType(MediaType.APPLICATION_JSON)
                .body(input).retrieve().body(RegisterOutputDTO.class);
    }

    public PaymentMethodInformationDTO getInfo(InfoCancelInputDTO input) {
        return getRestClient().post().uri("/" + MwpaymtConstant.INFO_PATH).contentType(MediaType.APPLICATION_JSON)
                .body(input).retrieve().body(PaymentMethodInformationDTO.class);
    }

    public DebitOutputDTO debit(DebitInputDTO input) {
        return getRestClient().post().uri("/" + MwpaymtConstant.DEBIT_PATH).contentType(MediaType.APPLICATION_JSON)
                .body(input).retrieve().body(DebitOutputDTO.class);
    }
}
