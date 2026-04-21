package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import static mc.gouv.xaf.back.dsp.utils.ResidUtils.convertMConnectDateToResidDate;
import static mc.gouv.xaf.back.dsp.utils.ResidUtils.convertMConnectDateToResidHourMinute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.dsp.dto.ResidCaisseOuverteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeCertificatResidenceCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeChangementSituationCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeDuplicataCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeNouvelleCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidDemandeRenouvellementCarteCompleteDTO;
import mc.gouv.xaf.back.dsp.dto.ResidHttpResponseDTO;
import mc.gouv.xaf.back.dsp.dto.ResidIdTSDTO;
import mc.gouv.xaf.back.dsp.dto.ResidInformationDebitDTO;
import mc.gouv.xaf.back.dsp.dto.ResidResidentCorrespondanceDTO;
import mc.gouv.xaf.back.dsp.dto.ResidStatutDemandeDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidInitialDemandeParamDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidApiService;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidErrorResponseErrorHandler;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.RestitutionStatistiquesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.RestitutionStatistiquesDTO;
import mc.gouv.xaf.shared.enums.SourceDonneesEnum;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class ResidApiServiceImpl implements ResidApiService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(ResidApiServiceImpl.class);
    private static final String URL_LOG = "URL: {} {}";
    private static final String HEADERS_LOG = "Headers: {}";

    // Entrypoints
    public static final String RESID_NOUVELLE_CARTE_PATH = "/demandes/nouvelleCarte";
    public static final String RESID_RENOUVELLEMENT_CARTE_PATH = "/demandes/renouvellementCarte";
    public static final String RESID_DUPLICATA_CARTE_PATH = "/demandes/duplicataCarte";
    public static final String RESID_CHANGEMENT_SITUATION_PATH = "/demandes/changementSituation";
    public static final String RESID_CERTIFICAT_RESIDENCE_PATH = "/demandes/certificatResidence";
    public static final String RESID_ETATS_DEMANDES_BY_ID_PATH = "/demandes/etatsDemandesById";
    public static final String RESID_USAGERS_PATH = "/usagers";
    public static final String RESID_NPDHL_PATH = "/npdhl";
    public static final String RESID_ETAT_CAISSE_PATH = "/caisse/ouverture";
    public static final String RESID_RETOUR_DEBIT_PATH = "/paiement/retourDebit";

    private final GouvPropertiesResolver gouvPropertiesResolver;

    private final FileService fileService;

    private final RestTemplateBuilder restTemplateBuilder;

    private final RestitutionStatistiquesService restitutionStatsService;

    private RestTemplate restTemplate;

    @Override
    public ResidHttpResponseDTO submitNouvelleCarteResid(ResidDemandeNouvelleCarteCompleteDTO nouvelleCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {

        LOGGER.info("Appel à l'API RESID pour la création d'une carte");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(nouvelleCarte,
                new ParameterizedTypeReference<>() {

                }, files, url, RESID_NOUVELLE_CARTE_PATH, jwt);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
            residHttpResponseDTO.setHttpStatus(201);
            return residHttpResponseDTO;
        }

        return responseEntity.getBody();
    }

    @Override
    public ResidHttpResponseDTO submitRenouvellementCarteResid(
            ResidDemandeRenouvellementCarteCompleteDTO renouvellement, Map<Integer, DemandeFileDTO> files, String url,
            String jwt) throws IOException {

        LOGGER.info("Appel à l'API RESID pour le renouvellement d'une carte");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(renouvellement,
                new ParameterizedTypeReference<>() {

                }, files, url, RESID_RENOUVELLEMENT_CARTE_PATH, jwt);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
            residHttpResponseDTO.setHttpStatus(201);
            ResidHttpResponseDTO body = responseEntity.getBody();
            if (null != body) {
                if (null != body.getMessage()) {
                    residHttpResponseDTO.setMessage(body.getMessage());
                }
                if (null != body.getWarnings() && !body.getWarnings().isEmpty()) {
                    residHttpResponseDTO.setWarnings(body.getWarnings());
                }
            }
            return residHttpResponseDTO;
        }
        return responseEntity.getBody();
    }

    @Override
    public ResidHttpResponseDTO submitDuplicataCarteResid(ResidDemandeDuplicataCarteCompleteDTO duplicataCarte,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {

        LOGGER.info("Appel à l'API RESID pour le duplicata d'une carte");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(duplicataCarte,
                new ParameterizedTypeReference<>() {

                }, files, url, RESID_DUPLICATA_CARTE_PATH, jwt);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
            residHttpResponseDTO.setHttpStatus(201);
            if (null != responseEntity.getBody()) {
                ResidHttpResponseDTO body = responseEntity.getBody();
                if (null != body) {
                    if (null != body.getMessage()) {
                        residHttpResponseDTO.setMessage(body.getMessage());
                    }
                    if (null != body.getWarnings() && !body.getWarnings().isEmpty()) {
                        residHttpResponseDTO.setWarnings(body.getWarnings());
                    }
                }
            }
            return residHttpResponseDTO;
        }

        return responseEntity.getBody();
    }

    @Override
    public ResidHttpResponseDTO submitChangementSituationResid(
            ResidDemandeChangementSituationCompleteDTO changementsituation, Map<Integer, DemandeFileDTO> files,
            String url, String jwt) throws IOException {

        LOGGER.info("Appel à l'API RESID pour le changement de situation");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(changementsituation,
                new ParameterizedTypeReference<ResidHttpResponseDTO>() {

                }, files, url, RESID_CHANGEMENT_SITUATION_PATH, jwt);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
            residHttpResponseDTO.setHttpStatus(201);
            ResidHttpResponseDTO body = responseEntity.getBody();
            if (null != body) {
                if (null != body.getMessage()) {
                    residHttpResponseDTO.setMessage(body.getMessage());
                }
                if (null != body.getWarnings() && !body.getWarnings().isEmpty()) {
                    residHttpResponseDTO.setWarnings(body.getWarnings());
                }
            }
            return residHttpResponseDTO;
        }

        return responseEntity.getBody();
    }

    @Override
    public ResidHttpResponseDTO submitCertificatResid(ResidDemandeCertificatResidenceCompleteDTO certificatResidence,
            Map<Integer, DemandeFileDTO> files, String url, String jwt) throws IOException {

        LOGGER.info("Appel à l'API RESID pour le certificat de residence");

        ResponseEntity<ResidHttpResponseDTO> responseEntity = submitDemandeResident(certificatResidence,
                new ParameterizedTypeReference<ResidHttpResponseDTO>() {

                }, files, url, RESID_CERTIFICAT_RESIDENCE_PATH, jwt);

        if (HttpStatus.CREATED.equals(responseEntity.getStatusCode())) {
            ResidHttpResponseDTO residHttpResponseDTO = new ResidHttpResponseDTO();
            residHttpResponseDTO.setHttpStatus(201);
            ResidHttpResponseDTO body = responseEntity.getBody();
            if (null != body) {
                if (null != body.getMessage()) {
                    residHttpResponseDTO.setMessage(body.getMessage());
                }
                if (null != body.getWarnings() && !body.getWarnings().isEmpty()) {
                    residHttpResponseDTO.setWarnings(body.getWarnings());
                }
            }
            return residHttpResponseDTO;
        }

        return responseEntity.getBody();
    }

    public <T, Y> ResponseEntity<Y> submitDemandeResident(T residObject, ParameterizedTypeReference<Y> type,
            Map<Integer, DemandeFileDTO> files, String residUrl, final String entryPoint, String jwt)
            throws IOException {

        MultiValueMap<String, Object> parts = this.createMultiparts(residObject, files);
        HttpHeaders headers = getResidMultipartRequestHeaders(jwt);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);

        RestTemplate rest = this.getRestTemplate();

        String requestUrl = residUrl + entryPoint;
        URI uri = UriComponentsBuilder.fromUriString(requestUrl).build().encode().toUri();

        LOGGER.debug("-- Appel RESID submit nouvelle carte");
        LOGGER.debug(URL_LOG, HttpMethod.POST, uri.toURL());
        LOGGER.debug(HEADERS_LOG, headers);
        String body = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(residObject);
        LOGGER.debug("Body: {}", body);

        ResponseEntity<Y> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity, type);

        // RESID Appel de l'API
        LOGGER.info("Fin appel RESID");

        return responseEntity;
    }

    private RestTemplate getRestTemplate() {
        if (restTemplate == null) {
            restTemplate = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler())
                    .requestFactory(() -> new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()))
                    .build();
            restTemplate.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        }
        return restTemplate;
    }

    private <T> MultiValueMap<String, Object> createMultiparts(T residObject, Map<Integer, DemandeFileDTO> files)
            throws IOException {
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();

        LOGGER.info("Création de la requête multipart");

        HttpHeaders requestHeadersJSON = new HttpHeaders();
        requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> residObjectJSONEntity = new HttpEntity<>(residObject, requestHeadersJSON);
        parts.add("demande", residObjectJSONEntity);

        LOGGER.info("Ajout des fichiers");

        for (Map.Entry<Integer, DemandeFileDTO> entry : files.entrySet()) {
            HttpHeaders requestHeadersAttachment = new HttpHeaders();
            ByteArrayResource fileAsResource = this.getByteArrayResource(entry);
            HttpEntity<ByteArrayResource> attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);
            parts.add("files", attachmentPart);
        }

        LOGGER.debug("Multiparts\n{}", parts);

        return parts;
    }

    private ByteArrayResource getByteArrayResource(Map.Entry<Integer, DemandeFileDTO> entry) throws IOException {
        String filePathEncoded = URLEncoder.encode(entry.getValue().getUrl(), StandardCharsets.UTF_8);
        InputStream isf = fileService.getFile(filePathEncoded, gouvPropertiesResolver.getContainerId());
        return new ByteArrayResource(IOUtils.toByteArray(isf)) {
            @Override
            public String getFilename() {
                // Format index-filename pour éviter les doublons de noms
                // ex. 1-Toto.txt
                return FileUtils.formatFilenameResid(entry.getValue().getName(), entry.getKey());
            }
        };
    }

    private HttpHeaders getResidMultipartRequestHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "multipart/form-data");
        headers.add("Accept", "*/*");
        headers.add("Authorization", "Bearer " + jwt);
        return headers;
    }

    protected HttpHeaders getResidRequestHeaders(String jwt) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", "Bearer " + jwt);
        return headers;
    }

    @Override
    public ResidStatutDemandeDTO getEtatDemande(ResidIdTSDTO idDemande, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException {
        LOGGER.info("Récupération du statut RESID de {}", idDemande);

        ResidStatutDemandeDTO statut = null;
        List<ResidStatutDemandeDTO> retList = getEtatMultipleDemandes(Collections.singletonList(idDemande), url, jwt);

        // On récupère uniquement le premier élément (on s'attend à ce qu'il y en ai maximum un)
        if (retList != null && !retList.isEmpty()) {
            statut = retList.getFirst();
        }
        return statut;
    }

    @Override
    public List<ResidStatutDemandeDTO> getEtatMultipleDemandes(List<ResidIdTSDTO> idsDemandes, String url, String jwt)
            throws JsonProcessingException, ResidHttpResponseException {
        LOGGER.info("Récupération des statuts RESID de {}", idsDemandes);

        // Construction du rest template
        RestTemplate rest = this.getRestTemplate();

        // Headers et URL
        HttpHeaders headers = getResidRequestHeaders(jwt);
        String requestUrl = url + RESID_ETATS_DEMANDES_BY_ID_PATH;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestUrl);

        // Construction de la requête
        HttpEntity<String> requestEntity = new HttpEntity<>(MAPPER.writeValueAsString(idsDemandes), headers);
        URI uri = builder.build().encode().toUri();

        // Logs DEBUG
        LOGGER.debug("-- Appel RESID Get état d'une demande");
        LOGGER.debug(URL_LOG, HttpMethod.POST, uri);
        LOGGER.debug(HEADERS_LOG, headers);
        String body = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(idsDemandes);
        LOGGER.debug("Body: {}", body);

        // Appel et réponse
        ResponseEntity<List<ResidStatutDemandeDTO>> responseEntity = rest.exchange(uri, HttpMethod.POST, requestEntity,
                new ParameterizedTypeReference<>() {

                });

        LOGGER.debug("Réponse de l'API {}", responseEntity.getBody());

        LOGGER.info("Fin de l'appel vers RESID pour la récupération du statut RESID de {}", idsDemandes);

        return responseEntity.getBody();
    }


    @Override
    public List<ResidResidentCorrespondanceDTO> getListResidCorrespondance(String numeroCarte, String url, String jwt)
            throws RestClientException {

        LOGGER.info("Appel à l'API RESID v2 /usagers pour demander les usagers correspondants");

        RestTemplate rest = new RestTemplate();
        rest.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));
        HttpHeaders headers = getResidRequestHeaders(jwt);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url + RESID_USAGERS_PATH)
                .queryParam("numeroCarte", numeroCarte);

        URI uri = builder.build().encode().toUri();

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        LOGGER.debug("-- Appel RESID Get liste usagers correspondance");
        LOGGER.debug(URL_LOG, HttpMethod.GET, uri);
        LOGGER.debug(HEADERS_LOG, headers);

        ResponseEntity<List<ResidResidentCorrespondanceDTO>> responseEntity = rest.exchange(uri, HttpMethod.GET,
                requestEntity, new ParameterizedTypeReference<>() {

                });

        LOGGER.info("Fin appel RESID getListResidCorrespondance");

        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            return Collections.emptyList();
        }

        return responseEntity.getBody();
    }

    @Override
    public ResidUsagerNpdhlDTO getUsagerDln1f(ResidInitialDemandeParamDTO paramDTO, String url, String jwt,
            Integer usagerId) throws ParseException {
        LOGGER.info("Appel à l'API RESID v2 /usagers/npdhl pour demander l'usager correspondant");
        RestTemplate rest = this.getRestTemplate();
        HttpHeaders headers = getResidRequestHeaders(jwt);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url + RESID_USAGERS_PATH + RESID_NPDHL_PATH)
                .queryParam("nom", paramDTO.getNom()).queryParam("prenoms", paramDTO.getPrenom())
                .queryParam("dateNaissance", convertMConnectDateToResidDate(paramDTO.getDateNaissance()))
                .queryParam("heureNaissance", convertMConnectDateToResidHourMinute(paramDTO.getDateNaissance()))
                .queryParam("villeNaissance", paramDTO.getVilleNaissance())
                .queryParam("paysNaissance", paramDTO.getPaysNaissance());
        URI uri = builder.build().encode().toUri();

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        LOGGER.debug("-- Appel RESID Get usager v2");
        LOGGER.debug(URL_LOG, HttpMethod.GET, uri);
        LOGGER.debug(HEADERS_LOG, headers);

        try {
            ResponseEntity<ResidUsagerNpdhlDTO> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity,
                    new ParameterizedTypeReference<>() {

                    });
            restitutionStatsService.saveRestitutionStatistique(
                    createStatsAStocker(HttpStatus.OK.value(), usagerId, ""));
            return responseEntity.getBody();
        } catch (Exception e) {
            LOGGER.error("====== ERREUR lors du GET Usager RESID", e);
            if (e.getCause() instanceof ResidHttpResponseException residException) {
                restitutionStatsService.saveRestitutionStatistique(
                        createStatsAStocker(residException.getHttpStatus(), usagerId, residException.getMessage()));
            }
        }

        LOGGER.info("Fin appel RESID getUsagerDln1f");
        return null;
    }

    @Override
    public ResidCaisseOuverteDTO getCaisseOuverte(String url, String jwt) {
        LOGGER.info("Appel à l'API RESID v2 /caisseOuverte pour connaitre le statut de la caisse");

        // Construction du rest template
        RestTemplate rest = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler()).build();
        rest.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // Headers et URL
        HttpHeaders headers = getResidRequestHeaders(jwt);
        String requestUrl = url + RESID_ETAT_CAISSE_PATH;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(requestUrl);

        // Construction de la requête
        URI uri = builder.build().encode().toUri();

        // Logs DEBUG
        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        LOGGER.debug("-- Appel RESID Get état de la caisse");
        LOGGER.debug(URL_LOG, HttpMethod.GET, uri);
        LOGGER.debug(HEADERS_LOG, headers);

        // Appel et réponse
        ResponseEntity<ResidCaisseOuverteDTO> responseEntity = rest.exchange(uri, HttpMethod.GET, requestEntity,
                new ParameterizedTypeReference<>() {

                });

        LOGGER.debug("Réponse de l'API {}", responseEntity.getBody());

        LOGGER.info("Fin de l'appel vers RESID pour la récupération du statut de la caisse");

        return responseEntity.getBody();
    }

    @Override
    public MultipartFile submitRetourDebit(ResidInformationDebitDTO informationDebit, String url, String jwt) throws IOException {

        LOGGER.info("Préparation de la requête à destination de RESID pour récupération de PDF");
        // Construction du rest template
        RestTemplate rest = restTemplateBuilder.errorHandler(new ResidErrorResponseErrorHandler()).build();
        rest.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));

        String requestUrl = url + RESID_RETOUR_DEBIT_PATH;
        URI uri = UriComponentsBuilder.fromUriString(requestUrl).build().encode().toUri();

        // ObjectMapper pour sérialiser l'objet
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody = mapper.writeValueAsString(informationDebit);

        // Construction du champ multipart contenant le JSON
        HttpHeaders partHeaders = new HttpHeaders();
        partHeaders.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> jsonPart = new HttpEntity<>(jsonBody, partHeaders);

        // Corps global de la requête multipart
        MultiValueMap<String, Object> multipartBody = new LinkedMultiValueMap<>();
        multipartBody.add("informationDebit", jsonPart);  //

        // Headers de la requête principale
        HttpHeaders headers = getResidRequestHeaders(jwt);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setAccept(List.of(MediaType.APPLICATION_PDF));  // PDF en réponse

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartBody, headers);

        // Logs DEBUG
        LOGGER.debug("-- Appel à RESID pour téléchargement de PDF");
        LOGGER.debug(URL_LOG, HttpMethod.POST, uri);
        LOGGER.debug(HEADERS_LOG, headers);
        LOGGER.debug("Body: {}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(informationDebit));

        // Appel HTTP
        ResponseEntity<byte[]> responseEntity = rest.exchange(
                uri,
                HttpMethod.POST,
                requestEntity,
                byte[].class
        );

        LOGGER.info("Fin de l'appel vers RESID pour téléchargement de PDF");

        if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
            byte[] pdfContent = responseEntity.getBody();
            String fileName = "recu_debit.pdf"; // ou autre nom
            return new MockMultipartFile(
                    fileName,
                    fileName,
                    MediaType.APPLICATION_PDF_VALUE,
                    pdfContent
            );
        } else {
            throw new IOException(
                    "Échec de la récupération du PDF depuis RESID. Statut: " + responseEntity.getStatusCode());
        }
    }

    private RestitutionStatistiquesDTO createStatsAStocker(Integer httpCode, Integer usagerId, String message) {
        RestitutionStatistiquesDTO statsAStocker = new RestitutionStatistiquesDTO();
        statsAStocker.setDate(new Date());
        statsAStocker.setHttpCode(httpCode);
        statsAStocker.setUsagerId(usagerId);
        statsAStocker.setMessage(message);
        statsAStocker.setSource(SourceDonneesEnum.RESID.name());
        statsAStocker.setDemarcheId(gouvPropertiesResolver.getDemarcheId());
        return statsAStocker;
    }
}
