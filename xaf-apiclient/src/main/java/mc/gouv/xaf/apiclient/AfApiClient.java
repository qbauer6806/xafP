package mc.gouv.xaf.apiclient;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import mc.gouv.xaf.shared.dto.*;
import mc.gouv.xboot.apiclient.authentication.impl.BasicAuthorizationHeaderProvider;
import mc.gouv.xboot.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xboot.apiclient.client.ApiClient;
import mc.gouv.xboot.apiclient.exception.ExceptionManager;
import org.glassfish.jersey.media.multipart.internal.MultiPartWriter;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Classe cliente permettant d'appeler les WS des démarches
 *
 * @author qdeme
 */
public class AfApiClient extends ApiClient {

    /**
     * Crée une instance du client avec sécurisation Basic Auth
     *
     * @param serviceUrl URL du WS à appeler
     * @param user       User à utiliser pour l'authentification
     * @param password   Mot de passe à utiliser pour l'authentification
     */
    public AfApiClient(String serviceUrl, String user, String password) {
        super(serviceUrl, new BasicAuthorizationHeaderProvider(user, password),
                ClientBuilder.newClient().register(JacksonJsonProvider.class).register(MultiPartWriter.class));
    }

    /**
     * Crée une instance du client avec sécurisation JWT
     *
     * @param serviceUrl URL du WS à appeler
     * @param jwtToken   JWT à utiliser pour l'authentification
     */
    public AfApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken),
                ClientBuilder.newClient().register(JacksonJsonProvider.class).register(MultiPartWriter.class));
    }

    public void annulerDemande(Integer demandeId, Integer usagerId) {
        Response res = getTarget().path("demandes/" + demandeId + "/annuler").queryParam("usagerId", usagerId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).put(Entity.text(""));

        ExceptionManager.checkExceptionResponse(res);

    }

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) {
        Response res = getTarget().path("demandes").queryParam("usagerId", usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(demande, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
                                                            DemandeComplementsReponseDTO reponse) {
        Response res = getTarget().path("demandes/" + demandeId + "/complements/" + icId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(reponse, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeComplementsDTO.class);
    }

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        Response res = getTarget().path("/usagers/" + usagerId + "/demandes/" + demandeId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public List<DemandeDTO> getDemandes(Integer usagerId) {
        Response res = getTarget().path("demandes").queryParam("usagerId", usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<DemandeDTO>>() {
        });
    }

    public Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO paramDTO) {
        Response res = getTarget().path("demandespage").queryParam("usagerId", usagerId).queryParam("page", paramDTO.getPage())
                .queryParam("size", paramDTO.getSize()).queryParam("sort", paramDTO.getSort()).queryParam("direction", paramDTO.getDirection())
                .queryParam("status", paramDTO.getStatus()).queryParam("lang", paramDTO.getLang())
                .request(MediaType.APPLICATION_JSON).header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();
        ExceptionManager.checkExceptionResponse(res);
        return res.readEntity(new GenericType<Page<DemandeDTO>>() {
        });
    }

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        Response res = getTarget().path("demandes/" + demandeId + "/complements/" + icId)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeComplementsDTO.class);
    }

    public List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId) {
        Response res = getTarget().path("demandes/" + demandeId + "/complements").request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<DemandeComplementsDTO>>() {
        });
    }

    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId) {
        Response res = getTarget().path("demandes/associerDemandeCourrier")
                .queryParam("identifiantDemande", identifiantDemande).queryParam("nomProprio", nomProprio)
                .queryParam("usagerId", usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).post(Entity.json(null));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(DemandeDTO.class);
    }

    public void desinscriptionUsager(Integer usagerId, String langue) {
        Response res = getTarget().path("/accesses/" + usagerId).queryParam("langue", langue)
                .request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        Response res = getTarget().path("/accesses/" + usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(dto, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(AccessDTO.class);
    }

    public AccessDTO getAccess(Integer usagerId) {
        Response res = getTarget().path("/accesses/" + usagerId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(AccessDTO.class);
    }

    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        Response res = getTarget().path("/usagerscourrier/" + usagerCourrierId).request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(UsagerCourrierDTO.class);
    }

    public List<MotifDTO> getMotifs() {
        Response res = getTarget().path("/motifs").request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<MotifDTO>>() {
        });
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        Response res = getTarget().path("/periodesouverture").request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<PeriodeOuvertureDTO>>() {
        });
    }

    public List<PropertiesDTO> getFrontProperties() {
        Response res = getTarget().path("/properties").request(MediaType.APPLICATION_JSON)
                .header("Authorization", getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<PropertiesDTO>>() {
        });
    }

}
