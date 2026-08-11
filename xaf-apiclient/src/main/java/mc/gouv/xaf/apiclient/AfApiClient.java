package mc.gouv.xaf.apiclient;

import java.util.List;
import java.util.Map;
import mc.gouv.xaf.apiclient.authentication.impl.JwtAuthorizationHeaderProvider;
import mc.gouv.xaf.apiclient.client.ApiClient;
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
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.UsagerCourrierDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

public class AfApiClient extends ApiClient {

    public AfApiClient(String serviceUrl, String jwtToken) {
        super(serviceUrl, new JwtAuthorizationHeaderProvider(jwtToken));
    }

    public void annulerDemande(Integer demandeId, Integer usagerId) {
        getRestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/annuler")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(demandeId))
                .contentType(MediaType.APPLICATION_JSON).body("").retrieve().toBodilessEntity();
    }

    public DemandeDTO creerDemande(DemandeInputDTO demande, Integer usagerId) {
        return getRestClient().post().uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH)
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build()).contentType(MediaType.APPLICATION_JSON)
                .body(demande).retrieve().body(DemandeDTO.class);
    }

    public DemandeDTO updateDemande(Integer demandeId, DemandeInputDTO demande, Integer usagerId) {
        return getRestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(demandeId))
                .contentType(MediaType.APPLICATION_JSON).body(demande).retrieve().body(DemandeDTO.class);
    }

    public DemandeDTO lockDemande(Integer demandeId, Integer usagerId, Long timestamp) {
        return getRestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/lock")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                        .queryParam(RequestConstant.TIMESTAMP_MODIFICATION, timestamp).build(demandeId))
                .contentType(MediaType.APPLICATION_JSON).body("").retrieve().body(DemandeDTO.class);
    }

    public DemandeDTO unlockDemande(Integer demandeId, Integer usagerId) {
        return getRestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/unlock")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(demandeId))
                .contentType(MediaType.APPLICATION_JSON).body("").retrieve().body(DemandeDTO.class);
    }

    public DemandeComplementsDTO repondreDemandeComplements(Integer demandeId, Integer icId,
            DemandeComplementsReponseDTO reponse) {
        return getRestClient().put()
                .uri("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/complements/{icId}", demandeId, icId)
                .contentType(MediaType.APPLICATION_JSON).body(reponse).retrieve().body(DemandeComplementsDTO.class);
    }

    public DemandeDTO getDemande(Integer usagerId, Integer demandeId) {
        return getRestClient().get()
                .uri("/usagers/{usagerId}/" + RequestConstant.DEMANDES_PATH + "/{demandeId}", usagerId, demandeId)
                .retrieve().body(DemandeDTO.class);
    }

    public byte[] getDemandeRecap(Integer usagerId, Integer demandeId, DonneesMConnectDTO donneesMConnectDTO) {
        RestClient.RequestBodySpec request = getRestClient().post()
                .uri("/usagers/{usagerId}/" + RequestConstant.DEMANDES_PATH + "/recap/{demandeId}", usagerId, demandeId)
                .contentType(MediaType.APPLICATION_JSON);
        if (donneesMConnectDTO != null) {
            request.body(donneesMConnectDTO);
        }
        return request.retrieve().body(byte[].class);
    }

    public Page<DemandeDTO> getDemandesPageable(Integer usagerId, PageParamDTO paramDTO) {
        return getRestClient().get().uri(uriBuilder -> {
            var builder = uriBuilder.path("/demandespage").queryParam(RequestConstant.USAGERID_PARAM, usagerId)
                    .queryParam("page", paramDTO.getPage()).queryParam("size", paramDTO.getSize())
                    .queryParam("sort", paramDTO.getSort()).queryParam("direction", paramDTO.getDirection())
                    .queryParam("lang", paramDTO.getLang());

            if (paramDTO.getStatus() != null) {
                paramDTO.getStatus().stream().filter(status -> status != null && !status.isEmpty())
                        .forEach(status -> builder.queryParam("status", status));
            }

            if (paramDTO.getStatusSimplifie() != null) {
                paramDTO.getStatusSimplifie().stream().filter(status -> status != null && !status.isEmpty())
                        .forEach(status -> builder.queryParam("statusSimplifie", status));
            }

            return builder.build();
        }).retrieve().body(new ParameterizedTypeReference<>() {

        });
    }

    public DemandeComplementsDTO getDemandeComplements(Integer demandeId, Integer icId) {
        return getRestClient().get()
                .uri("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/complements/{icId}", demandeId, icId)
                .retrieve().body(DemandeComplementsDTO.class);
    }

    public List<DemandeComplementsDTO> getDemandesComplements(Integer demandeId) {
        return getRestClient().get().uri("/" + RequestConstant.DEMANDES_PATH + "/{demandeId}/complements", demandeId)
                .retrieve().body(new ParameterizedTypeReference<>() {

                });
    }

    public DemandeDTO associerDemandeCourrier(String identifiantDemande, String nomProprio, Integer usagerId) {
        return getRestClient().post()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.DEMANDES_PATH + "/associerDemandeCourrier")
                        .queryParam("identifiantDemande", identifiantDemande).queryParam("nomProprio", nomProprio)
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build()).retrieve()
                .body(DemandeDTO.class);
    }

    public void desinscriptionUsager(Integer usagerId, String langue) {
        getRestClient().delete().uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.ACCESSES_PATH + "/{usagerId}")
                .queryParam("langue", langue).build(usagerId)).retrieve().toBodilessEntity();
    }

    public AccessDTO createOrUpdateAccess(Integer usagerId, AccessInputDTO dto) {
        return getRestClient().post().uri("/" + RequestConstant.ACCESSES_PATH + "/{usagerId}", usagerId)
                .contentType(MediaType.APPLICATION_JSON).body(dto).retrieve().body(AccessDTO.class);
    }

    public AccessDTO getAccess(Integer usagerId) {
        return getRestClient().get().uri("/" + RequestConstant.ACCESSES_PATH + "/{usagerId}", usagerId).retrieve()
                .body(AccessDTO.class);
    }

    public UsagerCourrierDTO getUsagerCourrier(Integer usagerCourrierId) {
        return getRestClient().get().uri("/usagerscourrier/{usagerCourrierId}", usagerCourrierId).retrieve()
                .body(UsagerCourrierDTO.class);
    }

    public List<MotifDTO> getMotifs() {
        return getRestClient().get().uri("/motifs").retrieve().body(new ParameterizedTypeReference<>() {

        });
    }

    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        return getRestClient().get().uri("/periodesouverture").retrieve().body(new ParameterizedTypeReference<>() {

        });
    }

    public JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params) {
        return getRestClient().get().uri(uriBuilder -> {
            var builder = uriBuilder.path("/donneesexternes").queryParam(RequestConstant.USAGERID_PARAM, usagerId);

            if (params != null) {
                params.forEach((key, values) -> {
                    if (values != null) {
                        for (String value : values) {
                            builder.queryParam(key, value);
                        }
                    }
                });
            }

            return builder.build();
        }).retrieve().body(JsonNode.class);
    }

    public List<PropertiesDTO> getFrontProperties() {
        return getRestClient().get().uri("/properties").retrieve().body(new ParameterizedTypeReference<>() {

        });
    }

    public BrouillonDTO creerBrouillon(BrouillonDTO brouillon, Integer usagerId) {
        return getRestClient().post().uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.BROUILLONS_PATH)
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build()).contentType(MediaType.APPLICATION_JSON)
                .body(brouillon).retrieve().body(BrouillonDTO.class);
    }

    public BrouillonDTO updateBrouillon(BrouillonDTO brouillon, Integer brouillonId, Integer usagerId) {
        return getRestClient().put()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.BROUILLONS_PATH + "/{brouillonId}")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(brouillonId))
                .contentType(MediaType.APPLICATION_JSON).body(brouillon).retrieve().body(BrouillonDTO.class);
    }

    public BrouillonDTO getBrouillon(Integer brouillonId, Integer usagerId) {
        return getRestClient().get()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.BROUILLONS_PATH + "/{brouillonId}")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(brouillonId)).retrieve()
                .body(BrouillonDTO.class);
    }

    public void deleteBrouillon(Integer brouillonId, Integer usagerId) {
        getRestClient().delete()
                .uri(uriBuilder -> uriBuilder.path("/" + RequestConstant.BROUILLONS_PATH + "/{brouillonId}")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).build(brouillonId)).retrieve()
                .toBodilessEntity();
    }

    public void deleteFile(String fileUrl) {
        getRestClient().delete().uri("/file/" + fileUrl).retrieve().toBodilessEntity();
    }

    public Page<BrouillonDTO> getBrouillonsPageable(Integer usagerId, PageParamDTO paramDTO) {
        return getRestClient().get().uri(uriBuilder -> uriBuilder.path("/brouillonspage")
                        .queryParam(RequestConstant.USAGERID_PARAM, usagerId).queryParam("page", paramDTO.getPage())
                        .queryParam("size", paramDTO.getSize()).queryParam("sort", paramDTO.getSort())
                        .queryParam("direction", paramDTO.getDirection()).build()).retrieve()
                .body(new ParameterizedTypeReference<>() {

                });
    }

    public JsonNode creerConfig(JsonNode config) {
        return getRestClient().post().uri("/" + RequestConstant.CONFIGS_PATH).contentType(MediaType.APPLICATION_JSON)
                .body(config).retrieve().body(JsonNode.class);
    }

    public List<PaysDTO> getPays() {
        return getRestClient().get().uri("/pays").retrieve().body(new ParameterizedTypeReference<>() {

        });
    }
}
