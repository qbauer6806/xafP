package mc.gouv.xaf.back.service.itg.sms.impl;

import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.back.service.itg.sms.SmsDTO;

/**
 * Classe cliente permettant d'appeler l'API SMS
 *
 * @author qdeme
 */
public class SmsClient extends ApiClient {
	
	public static final String SMS_PATH = "sms";

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *            URL du WS à appeler
     * @param jwtToken
     *            JWT à utiliser pour l'authentification
     */
    public SmsClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken),
                ClientBuilder.newClient().register(JacksonJsonProvider.class).register(MultiPartWriter.class));
    }

    public SmsDTO sendSms(SmsDTO sms) {
        Response res = getTarget().path(SMS_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(sms, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(SmsDTO.class);
    }

    public SmsDTO getSms(String identifiant) {
        Response res = getTarget().path(SMS_PATH + "/" + identifiant)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(SmsDTO.class);
    }

}
