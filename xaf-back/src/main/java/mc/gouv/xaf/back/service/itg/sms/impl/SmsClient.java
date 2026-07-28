package mc.gouv.xaf.back.service.itg.sms.impl;

import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.back.service.itg.sms.dto.SmsDTO;
import org.springframework.http.MediaType;

public class SmsClient extends ApiClient {

    public static final String SMS_PATH = "sms";

    public SmsClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken));
    }

    public SmsDTO sendSms(SmsDTO sms) {
        return getRestClient().post()
                .uri("/" + SMS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(sms)
                .retrieve()
                .body(SmsDTO.class);
    }

    public SmsDTO getSms(String identifiant) {
        return getRestClient().get()
                .uri("/" + SMS_PATH + "/{identifiant}", identifiant)
                .retrieve()
                .body(SmsDTO.class);
    }
}
