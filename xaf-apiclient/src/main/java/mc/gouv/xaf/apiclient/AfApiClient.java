package mc.gouv.xaf.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient.authentication.impl.BasicAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
import mc.gouv.xaf.apiclient.exception.ExceptionManager;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.AccessInputDTO;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.Page;
import mc.gouv.xaf.shared.dto.PageParamDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;

/**
 * Classe cliente permettant d'appeler les WS des démarches
 *
 * @author qdeme
 */
public class AfApiClient extends ApiClient {

    /**
     * Crée une instance du client avec sécurisation Basic Auth
     *
     * @param serviceUrl
     *         URL du WS à appeler
     * @param user
     *         User à utiliser pour l'authentification
     * @param password
     *         Mot de passe à utiliser pour l'authentification
     */
    public AfApiClient(String serviceUrl, String user, String password) {
        super(serviceUrl, new BasicAuthorizationHeaderProvider(user, password), true);
    }


    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl
     *         URL du WS à appeler
     * @param jwtToken
     *         JWT à utiliser pour l'authentification
     */
    public AfApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken), true);
    }

    public void annulerDemande(Integer demandeId, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/annuler")
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.text(""));

        ExceptionManager.checkExceptionResponse(res);
    }

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(demande, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(demande, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/lock")
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .queryParam(RequestConstant.TIMESTAMP_MODIFICATION, timestamp).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity("", MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/unlock")
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity("", MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/complements/" + icId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(reponse, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeComplementsDTO.class);
    }

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        Response res = getTarget().path("/usagers/" + usagerId + '/' + RequestConstant.DEMANDES_PATH + '/' + demandeId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO) {
        Response res = getTarget().path(
                        "/usagers/" + usagerId + '/' + RequestConstant.DEMANDES_PATH + "/recap/" + demandeId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(donneesMConnectDTO, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(byte[].class);
    }

    public List<DemandeDTO> getDemandes(Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<DemandeDTO>>() {

        });
    }

    public Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO paramDTO) {
        Response res = getTarget().path("demandespage").queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .queryParam("page", paramDTO.getPage()).queryParam("size", paramDTO.getSize())
                .queryParam("sort", paramDTO.getSort()).queryParam("direction", paramDTO.getDirection())
                .queryParam("status", paramDTO.getStatus()).queryParam("lang", paramDTO.getLang())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<Page<DemandeDTO>>() {

        });
    }

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/complements/" + icId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeComplementsDTO.class);
    }

    public List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + '/' + demandeId + "/complements")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<DemandeComplementsDTO>>() {

        });
    }

    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.DEMANDES_PATH + "/associerDemandeCourrier")
                .queryParam("identifiantDemande", identifiantDemande).queryParam("nomProprio", nomProprio)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(null));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public void desinscriptionUsager(Integer usagerId, String langue) {
        Response res = getTarget().path('/' + RequestConstant.ACCESSES_PATH + '/' + usagerId)
                .queryParam("langue", langue).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        Response res = getTarget().path('/' + RequestConstant.ACCESSES_PATH + '/' + usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(dto, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(AccessDTO.class);
    }

    public AccessDTO getAccess(Integer usagerId) {
        Response res = getTarget().path('/' + RequestConstant.ACCESSES_PATH + '/' + usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(AccessDTO.class);
    }

    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        Response res = getTarget().path("/usagerscourrier/" + usagerCourrierId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(UsagerCourrierDTO.class);
    }

    public List<MotifDTO> getMotifs() {
        Response res = getTarget().path("/motifs").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<MotifDTO>>() {

        });
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        Response res = getTarget().path("/periodesouverture").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<PeriodeOuvertureDTO>>() {

        });
    }

    public JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params) {

        WebTarget webTarget = getTarget();
        if (params != null) {
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                if (entry.getValue() != null) {
                    for (String str : entry.getValue()) {
                        webTarget = webTarget.queryParam(entry.getKey(), str);
                    }
                }
            }
        }

        Response res = webTarget.path("/donneesexternes").queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<JsonNode>() {

        });
    }

    public List<PropertiesDTO> getFrontProperties() {
        Response res = getTarget().path("/properties").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<PropertiesDTO>>() {

        });
    }

    public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.BROUILLONS_PATH)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(brouillon, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(BrouillonDTO.class);
    }

    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer brouillonId, Integer usagerId) {
        Response res = getTarget().path(RequestConstant.BROUILLONS_PATH + '/' + brouillonId)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(brouillon, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(BrouillonDTO.class);
    }

    public BrouillonDTO getBrouillon(Integer brouillonId, Integer usagerId) {
        Response res = getTarget().path('/' + RequestConstant.BROUILLONS_PATH + '/' + brouillonId)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(BrouillonDTO.class);
    }

    public void deleteBrouillon(Integer brouillonId, Integer usagerId) {
        Response res = getTarget().path('/' + RequestConstant.BROUILLONS_PATH + '/' + brouillonId)
                .queryParam(RequestConstant.USAGERID_PARAM, usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public void deleteFile(String fileUrl) {
        Response res = getTarget().path("/file/" + fileUrl).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
        Response res = getTarget().path("brouillonspage").queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                .queryParam("page", paramDTO.getPage()).queryParam("size", paramDTO.getSize())
                .queryParam("sort", paramDTO.getSort()).queryParam("direction", paramDTO.getDirection())
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<Page<BrouillonDTO>>() {

        });
    }

    public JsonNode creerConfig(JsonNode config) {
        Response res;
        try {
            res = getTarget().path(RequestConstant.CONFIGS_PATH).request(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                    .post(Entity.entity(config, MediaType.APPLICATION_JSON));

            ExceptionManager.checkExceptionResponse(res);
        } catch (Exception e) {
            return null;
        }

        return res.readEntity(new GenericType<>() {

        });
    }

}
