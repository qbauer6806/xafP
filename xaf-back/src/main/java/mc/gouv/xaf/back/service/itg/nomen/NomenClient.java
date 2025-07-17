package mc.gouv.xaf.back.service.itg.nomen;

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

    public static final String NOMEN_PATH = "nomenclatures";

    public static final String VALEUR_PATH = "/valeurs";

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *            URL du WS à appeler
     * @param jwtToken
     *            JWT à utiliser pour l'authentification
     */
    public NomenClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken));
    }

    public NomenNomenclatureDTO getNomenclature(String identifiant) {
        Response res = getTarget().path(NOMEN_PATH + "/" + identifiant + VALEUR_PATH)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureAvecLocale(String identifiant, String locale) {
        Response res = getTarget().path(NOMEN_PATH + "/" + identifiant + VALEUR_PATH).queryParam("valeurLangue", locale)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureValeur(String identifiant, String valeur) {
        Response res = getTarget().path(NOMEN_PATH + "/" + identifiant + VALEUR_PATH).queryParam("valeurCode", valeur)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }

    public NomenNomenclatureDTO getNomenclatureValeurAvecLocale(String identifiant, String valeur, String locale) {
        Response res = getTarget().path(NOMEN_PATH + "/" + identifiant + VALEUR_PATH).queryParam("valeurCode", valeur)
                .queryParam("valeurLangue", locale).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(NomenNomenclatureDTO.class);
    }

}
