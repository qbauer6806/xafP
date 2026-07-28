package mc.gouv.xaf.apiclient2tiers;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient2tiers.authentication.impl.BasicAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient2tiers.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient2tiers.client.ApiClient;
import mc.gouv.xaf.apiclient2tiers.dto.FileResponseDTO;
import mc.gouv.xaf.apiclient2tiers.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.apiclient2tiers.dto.RecapDemandesDTO;
import mc.gouv.xaf.apiclient2tiers.dto.StatutSimplifieEnum;
import mc.gouv.xaf.apiclient2tiers.dto.UsagerDemandesRecapDTO;
import mc.gouv.xaf.shared.exception.DemarcheException;
import org.apache.commons.io.IOUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import tools.jackson.databind.json.JsonMapper;

/**
 * Classe cliente permettant au système tiers d'appeler l'API GenTS via le FO (solution 2/3 "GenTS Connect")
 *
 * @author qdeme
 */
public class AfApiClient2Tiers extends ApiClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

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
        super(serviceUrl, new BasicAuthorizationHeaderProvider(user, password));
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
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken));
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        return getRestClient().get().uri("/periodesouverture").retrieve().body(new ParameterizedTypeReference<>() {

        });
    }

    public PeriodeOuvertureDTO createPeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
        return getRestClient().post().uri("/periodesouverture").contentType(MediaType.APPLICATION_JSON)
                .body(periodeOuverture).retrieve().body(PeriodeOuvertureDTO.class);
    }

    public PeriodeOuvertureDTO updatePeriodeOuverture(Integer pkPeriodeOuverture,
            PeriodeOuvertureDTO periodeOuverture) {
        return getRestClient().put().uri("/periodesouverture/{pkPeriodeOuverture}", pkPeriodeOuverture)
                .contentType(MediaType.APPLICATION_JSON).body(periodeOuverture).retrieve()
                .body(PeriodeOuvertureDTO.class);
    }

    public void deletePeriodeOuverture(Integer pkPeriodeOuverture) {
        getRestClient().delete().uri("/periodesouverture/{pkPeriodeOuverture}", pkPeriodeOuverture).retrieve()
                .toBodilessEntity();
    }

    public String saveFile(InputStream inputStream, Integer usagerId, String filename, String contentType,
            Map<String, String> customHeaders) {

        // Constitution du chemin virtuel du fichier, sans le container, car il a été décidé qu'il ne serait plus fourni
        // à l'API GenTS et que c'est cette dernière qui rajouterait "ROOT" par défaut lors de l'appel à FILE
        String virtualPath = filename;

        // Ajout du contenu multipart
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.parseMediaType(contentType));

        HttpEntity<InputStreamResource> filePart = new HttpEntity<>(new InputStreamResource(inputStream) {

            @Override
            public String getFilename() {
                return filename;
            }
        }, fileHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("data", filePart);

        return getRestClient().post().uri("/file/{usagerId}/{virtualPath}", usagerId, virtualPath)
                .contentType(MediaType.MULTIPART_FORM_DATA).accept(MediaType.APPLICATION_JSON).headers(headers -> {
                    // Si le client a fourni des métadonnées (en X-MC-*), alors les transmettre à FILE
                    if (customHeaders != null) {
                        customHeaders.forEach((headerName, headerValue) -> {
                            if (headerName != null && headerName.startsWith(MC_METADATA_PREFIX)) {
                                headers.add(headerName, headerValue);
                            }
                        });
                    }
                }).body(body).exchange((request, response) -> {
                    byte[] responseBody = response.getBody().readAllBytes();

                    if (!response.getStatusCode().isSameCodeAs(org.springframework.http.HttpStatus.CREATED)) {
                        throw new DemarcheException(new String(responseBody));
                    }

                    FileResponseDTO fileResponse = MAPPER.readValue(responseBody, FileResponseDTO.class);
                    return fileResponse.getMessage();
                });
    }

    public void getFile(String file, HttpServletResponse response) {

        // Appel du WS FILE
        getRestClient().get().uri("/file/{file}", file).exchange((request, remoteResponse) -> {

            // Constitution de la réponse pour retour au client
            response.setStatus(remoteResponse.getStatusCode().value());

            MediaType contentType = remoteResponse.getHeaders().getContentType();
            if (contentType != null) {
                response.setContentType(contentType.toString());
            }

            // Copie des headers
            remoteResponse.getHeaders()
                    .forEach((headerName, values) -> values.forEach(value -> response.addHeader(headerName, value)));

            // Copie du contenu de l'entité de réponse dans le flux de sortie de la réponse HTTP
            try {
                IOUtils.copy(remoteResponse.getBody(), response.getOutputStream());
            } catch (IOException e) {
                throw new DemarcheException(
                        "Erreur lors de la copie du contenu de l'entité de réponse : " + e.getMessage());
            }

            return null;
        });
    }

    public void deleteFile(String file) {
        // Appel du WS FILE
        getRestClient().delete().uri("/file/{file}", file).exchange((request, remoteResponse) -> {
            if (!remoteResponse.getStatusCode().is2xxSuccessful()) {
                String errorMessage = new String(remoteResponse.getBody().readAllBytes());
                throw new DemarcheException(errorMessage);
            }
            return null;
        });
    }

    public void notifyCreationDemande(Integer usagerId, Integer pkDemande, String identifiantDemande, Date dateCreation,
            RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateCreation);

        getRestClient().post().uri(uriBuilder -> uriBuilder.path(NOTIFY + "{usagerId}/creationDemande")
                        .queryParam(DEMANDE_ID, pkDemande).queryParam(IDENTIFIANT_DEMANDE, identifiantDemande)
                        .queryParam("dateCreation", formattedDate).build(usagerId)).contentType(MediaType.APPLICATION_JSON)
                .body(recapDemandes).retrieve().toBodilessEntity();
    }

    public void notifyChangementStatutDemande(Integer usagerId, Integer pkDemande, String identifiantDemande,
            StatutSimplifieEnum statutSimplifie, Date dateStatutSimplifie, RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateStatutSimplifie);

        getRestClient().post().uri(uriBuilder -> uriBuilder.path(NOTIFY + "{usagerId}/changementStatutDemande")
                        .queryParam(DEMANDE_ID, pkDemande).queryParam(IDENTIFIANT_DEMANDE, identifiantDemande)
                        .queryParam("statutSimplifie", statutSimplifie.name()).queryParam("dateStatutSimplifie", formattedDate)
                        .build(usagerId)).contentType(MediaType.APPLICATION_JSON).body(recapDemandes).retrieve()
                .toBodilessEntity();
    }

    public void notifySuppressionDemande(Integer usagerId, Integer pkDemande, String identifiantDemande,
            Date dateSuppression, RecapDemandesDTO recapDemandes) {

        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        String formattedDate = dateFormat.format(dateSuppression);

        getRestClient().post().uri(uriBuilder -> uriBuilder.path(NOTIFY + "{usagerId}/suppressionDemande")
                        .queryParam(DEMANDE_ID, pkDemande).queryParam(IDENTIFIANT_DEMANDE, identifiantDemande)
                        .queryParam("dateSuppression", formattedDate).build(usagerId)).contentType(MediaType.APPLICATION_JSON)
                .body(recapDemandes).retrieve().toBodilessEntity();
    }

    public void synchronizeDemandesRecaps(List<UsagerDemandesRecapDTO> usagerDemandesRecap) {
        getRestClient().post().uri("/notify/synchronizeDemandesRecaps").contentType(MediaType.APPLICATION_JSON)
                .body(usagerDemandesRecap).retrieve().toBodilessEntity();
    }
}
