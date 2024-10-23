package mc.gouv.xaf.apiclient2tiers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient2tiers.authentication.impl.BasicAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient2tiers.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient2tiers.client.ApiClient;
import mc.gouv.xaf.apiclient2tiers.dto.GichuniUsagerDTO;
import mc.gouv.xaf.apiclient2tiers.dto.MotifDTO;
import mc.gouv.xaf.apiclient2tiers.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.apiclient2tiers.dto.RecapDemandesDTO;
import mc.gouv.xaf.apiclient2tiers.dto.StatutSimplifieEnum;
import mc.gouv.xaf.apiclient2tiers.dto.UsagerDemandesRecapDTO;
import mc.gouv.xaf.apiclient2tiers.exception.ExceptionManager;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

/**
 * Classe cliente permettant d'appeler les WS des démarches
 *
 * @author qdeme
 */
public class AfApiClient2Tiers extends ApiClient {

    private static final String MC_METADATA_PREFIX = "X-MC-";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String NOTIFY = "/notify/";
    private static final String DEMANDE_ID = "demandeId";
    private static final String IDENTIFIANT_DEMANDE = "identifiantDemande";

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
    public AfApiClient2Tiers(String serviceUrl, String user, String password) {
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
    public AfApiClient2Tiers(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken), true);
    }

    public List<MotifDTO> getMotifs() {
        Response res = getTarget().path("/motifs").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<MotifDTO>>() {

        });
    }

    public MotifDTO createMotif(MotifDTO motif) {
        Response res = getTarget().path("/motifs").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(motif, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(MotifDTO.class);
    }

    public MotifDTO updateMotif(Integer pkMotif, MotifDTO motif) {
        Response res = getTarget().path("/motifs/" + pkMotif).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(motif, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(MotifDTO.class);
    }

    public void deleteMotif(Integer pkMotif) {
        Response res = getTarget().path("/motifs/" + pkMotif).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        Response res = getTarget().path("/periodesouverture").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(new GenericType<List<PeriodeOuvertureDTO>>() {

        });
    }

    public PeriodeOuvertureDTO createPeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
        Response res = getTarget().path("/periodesouverture").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(periodeOuverture, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(PeriodeOuvertureDTO.class);
    }

    public PeriodeOuvertureDTO updatePeriodeOuverture(Integer pkPeriodeOuverture,
            PeriodeOuvertureDTO periodeOuverture) {
        Response res = getTarget().path("/periodesouverture/" + pkPeriodeOuverture).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .put(Entity.entity(periodeOuverture, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(PeriodeOuvertureDTO.class);
    }

    public void deletePeriodeOuverture(Integer pkPeriodeOuverture) {
        Response res = getTarget().path("/periodesouverture/" + pkPeriodeOuverture).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).delete();

        ExceptionManager.checkExceptionResponse(res);
    }

    public String saveFile(String container, InputStream inputStream, String filename, String contentType,
            Map<String, String> customHeaders) {

        // Constitution du chemin virtuel du fichier
        // /appfactory/demarcheId/accessId/UUID/nomDuFichier
        String virtualPath = container + "/" + filename;

        // Constitution de la requête
        Invocation.Builder builder = getTarget().path("/file/" + virtualPath).request(MediaType.MULTIPART_FORM_DATA);

        // Ajout du contenu multipart
        MultiPart multiPart = new MultiPart();
        multiPart.setMediaType(MediaType.MULTIPART_FORM_DATA_TYPE);
        multiPart.bodyPart(new StreamDataBodyPart("data", inputStream, filename, MediaType.valueOf(contentType)));
        Entity<MultiPart> entity = Entity.entity(multiPart, multiPart.getMediaType());

        // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
        if (customHeaders != null) {
            MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
            for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
                String headerName = entry.getKey();
                String headerValue = entry.getValue();
                if (headerName.startsWith(MC_METADATA_PREFIX)) {
                    headers.add(headerName, headerValue);
                }
            }
            builder.headers(headers);
        }

        builder.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);

        builder.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue());

        try (Response postResponse = builder.post(entity)) {
            // Gestion des erreurs
            int statusCode = postResponse.getStatus();
            if (statusCode != Response.Status.CREATED.getStatusCode()) {
                String errorMessage = postResponse.readEntity(String.class);
                throw new DemarcheException(errorMessage);
            }
        }

        return filename;
    }

    public void getFile(String file, HttpServletResponse response) {

        // Préparation de la requête
        Invocation.Builder builder = getTarget().path("/file/ROOT/" + file).request();
        builder.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue());

        // Appel du WS FILE
        Response remoteResponse = builder.get();

        // Constitution de la réponse pour retour au client
        response.setStatus(remoteResponse.getStatus());
        response.setContentType(remoteResponse.getMediaType().toString());

        // Copie des headers
        for (String headerName : remoteResponse.getHeaders().keySet()) {
            for (Object value : remoteResponse.getHeaders().get(headerName)) {
                response.addHeader(headerName, value.toString());
            }
        }

        // Copie du contenu de l'entité de réponse dans le flux de sortie de la réponse HTTP
        try {
            IOUtils.copy(remoteResponse.readEntity(InputStream.class), response.getOutputStream());
        } catch (IOException e) {
            throw new DemarcheException(
                    "Erreur lors de la copie du contenu de l'entité de réponse : " + e.getMessage());
        }
    }

    public void deleteFile(String file) {
        // Préparation de la requête
        Invocation.Builder builder = getTarget().path("/file/ROOT/" + file).request();
        builder.header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue());

        // Appel du WS FILE
        try (Response remoteResponse = builder.delete()) {
            int statusCode = remoteResponse.getStatus();
            if (statusCode != Response.Status.OK.getStatusCode()) {
                String errorMessage = remoteResponse.readEntity(String.class);
                throw new DemarcheException(errorMessage);
            }
        }

    }

    public GichuniUsagerDTO getUsager(Integer usagerId) {
        Response res = getTarget().path("/usagers/" + usagerId).request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue()).get();

        ExceptionManager.checkExceptionResponse(res);

        return res.readEntity(GichuniUsagerDTO.class);
    }

    public void notifyCreationDemande(Integer usagerId, Integer pkDemande, String identifiantDemande, Date dateCreation,
            RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateCreation);

        Response res = getTarget().path(NOTIFY + usagerId + "/creationDemande").queryParam(DEMANDE_ID, pkDemande)
                .queryParam(IDENTIFIANT_DEMANDE, identifiantDemande).queryParam("dateCreation", formattedDate)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(recapDemandes, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
    }

    public void notifyChangementStatutDemande(Integer usagerId, Integer pkDemande, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateStatutSimplifie);

        Response res = getTarget().path(NOTIFY + usagerId + "/changementStatutDemande")
                .queryParam(DEMANDE_ID, pkDemande).queryParam(IDENTIFIANT_DEMANDE, identifiantDemande)
                .queryParam("statutSimplifie", statutSimplifie.name()).queryParam("dateStatutSimplifie", formattedDate)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(recapDemandes, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
    }

    public void notifySuppressionDemande(Integer usagerId, Integer pkDemande, String identifiantDemande,
            Date dateSuppression, RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateSuppression);

        Response res = getTarget().path(NOTIFY + usagerId + "/suppressionDemande").queryParam(DEMANDE_ID, pkDemande)
                .queryParam(IDENTIFIANT_DEMANDE, identifiantDemande).queryParam("dateSuppression", formattedDate)
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(recapDemandes, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
    }

    public void notifyDesinscriptionUsagerTS(Integer usagerId) {
        Response res = getTarget().path(NOTIFY + usagerId + "/desinscriptionUsagerTS")
                .request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(""));

        ExceptionManager.checkExceptionResponse(res);
    }

    public void synchronizeDemandesRecaps(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        Response res = getTarget().path("/notify/synchronizeDemandesRecaps").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.entity(usagerDemandesRecap, MediaType.APPLICATION_JSON));

        ExceptionManager.checkExceptionResponse(res);
    }

    public void notifyCreationAccesTS(Integer usagerId) {
        Response res = getTarget().path(NOTIFY + usagerId + "/creationAccesTS").request(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, getAuthorizationHeaderProvider().getHeaderValue())
                .post(Entity.json(""));

        ExceptionManager.checkExceptionResponse(res);
    }

}
