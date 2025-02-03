package mc.gouv.xaf.back.service.itg.nomen;

import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;

import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.back.service.itg.nomen.dto.NomenNomenclatureDTO;

/**
 * Client permettant d'appeler l'API NOMEN
 * 
 * @author qdeme
 * 
 */
public class NomenClient extends ApiClient {
	
	public static final String SMS_PATH = "nomenclatures";

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *            URL du WS à appeler
     * @param jwtToken
     *            JWT à utiliser pour l'authentification
     */
    public NomenClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken),
                ClientBuilder.newClient().register(JacksonJsonProvider.class).register(MultiPartWriter.class));
    }

    public NomenNomenclatureDTO getNomenclature(String identifiant) {
        Response res = getTarget().path(SMS_PATH + "/" + identifiant + "/valeurs")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }
    
    public NomenNomenclatureDTO getNomenclatureAvecLocale(String identifiant, String locale) {
        Response res = getTarget().path(SMS_PATH + "/" + identifiant + "/valeurs")
        		.queryParam("valeurLangue", locale)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }
    
    public NomenNomenclatureDTO getNomenclatureValeur(String identifiant, String valeur) {
        Response res = getTarget().path(SMS_PATH + "/" + identifiant + "/valeurs")
        		.queryParam("valeurCode", valeur)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }
    
    public NomenNomenclatureDTO getNomenclatureValeurAvecLocale(String identifiant, String valeur, String locale) {
        Response res = getTarget().path(SMS_PATH + "/" + identifiant + "/valeurs")
        		.queryParam("valeurCode", valeur)
        		.queryParam("valeurLangue", locale)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }

}
