package mc.gouv.xaf.back.dsp.service.itg.resid.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidInitialDemandeParamDTO;
import mc.gouv.xaf.back.dsp.dto.dlnuf.ResidUsagerNpdhlDTO;
import mc.gouv.xaf.back.dsp.exception.ResidHttpResponseException;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidApiService;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidInitialDemandeMapper;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidInitialDemandeService;
import mc.gouv.xaf.back.dsp.service.itg.resid.ResidPropertiesResolver;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DonneesExternesDemandeDTO;
import mc.gouv.xaf.shared.enums.SourceDonneesEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@Component
public class ResidInitialDemandeServiceImpl implements ResidInitialDemandeService {

    public static final String DONNEES_EXTERNES_MCONNECT_GIVENNAME = "GivenName";
    public static final String DONNEES_EXTERNES_MCONNECT_FAMILYNAME = "FamilyName";
    public static final String DONNEES_EXTERNES_MCONNECT_BIRTHNAME = "BirthName";
    public static final String DONNEES_EXTERNES_MCONNECT_BIRTHDATE = "BirthDatetime";
    public static final String DONNEES_EXTERNES_MCONNECT_BIRTHPLACECITY = "birthPlaceCity";
    public static final String DONNEES_EXTERNES_MCONNECT_BIRTHPLACECOUNTRY = "birthPlaceCountry";
    public static final String DONNEES_EXTERNES_USAGER_INFO_EMAIL = "usagerInfoEmail";
    public static final String DONNEES_EXTERNES_USAGER_INFO_TITRE = "usagerInfoTitre";
    private static final Logger LOGGER = LoggerFactory.getLogger(ResidInitialDemandeServiceImpl.class);
    @Autowired
    private ResidApiService residApiService;
    @Autowired
    private ResidPropertiesResolver residPropertiesResolver;
    @Autowired
    private ResidInitialDemandeMapper<?> residInitialDemandeMapper;

    static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JavaTimeModule()); // pour la gestion des OffsetDateTime
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonNode getInitialDemande(Integer usagerId, Map<String, String[]> params) throws ResidHttpResponseException, ParseException, JsonProcessingException {
        if(!this.isMconnectCall(params)){
            LOGGER.info("Impossible d'appeler RESID avec les données fournies");
            DonneesExternesDemandeDTO donneesExternesDemandeDTO = new DonneesExternesDemandeDTO();
            donneesExternesDemandeDTO.setDemande(null);
            donneesExternesDemandeDTO.setStatut(DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.NOK);
            return mapper.valueToTree(donneesExternesDemandeDTO);
        }
        LOGGER.info("Début de récupération des données depuis RESID");
        String jwt = residPropertiesResolver.getResidApiJwt();
        if(StringUtils.isBlank(jwt)){
            throw new ResidHttpResponseException("Le TOKEN pour l'appel à RESID est null");
        }
        String residApiUrlV2 = residPropertiesResolver.getResidApiUrlV2();
        if(StringUtils.isBlank(residApiUrlV2)){
            throw new ResidHttpResponseException("L'url pour l'appel à RESID est null");
        }
        LOGGER.info("URL RESID: {}", residApiUrlV2);

        String dateNaissance = params.get(DONNEES_EXTERNES_MCONNECT_BIRTHDATE)[0];
        String nom = params.get(DONNEES_EXTERNES_MCONNECT_BIRTHNAME)[0];
        String prenom = params.get(DONNEES_EXTERNES_MCONNECT_GIVENNAME)[0];
        String villeNaissance = params.get(DONNEES_EXTERNES_MCONNECT_BIRTHPLACECITY)[0];
        String paysNaissance = params.get(DONNEES_EXTERNES_MCONNECT_BIRTHPLACECOUNTRY)[0];
        String email = params.get(DONNEES_EXTERNES_USAGER_INFO_EMAIL)[0];
        String titre = params.get(DONNEES_EXTERNES_USAGER_INFO_TITRE)[0];

        ResidInitialDemandeParamDTO initialDemandeDTO = new ResidInitialDemandeParamDTO();
        initialDemandeDTO.setNom(nom);
        initialDemandeDTO.setNomusage(params.get(DONNEES_EXTERNES_MCONNECT_FAMILYNAME)[0]);
        initialDemandeDTO.setPrenom(prenom);
        initialDemandeDTO.setDateNaissance(dateNaissance);
        initialDemandeDTO.setVilleNaissance(villeNaissance);
        initialDemandeDTO.setPaysNaissance(paysNaissance);
        initialDemandeDTO.setEmail(email);
        initialDemandeDTO.setTitre(titre);

        ResidUsagerNpdhlDTO usagerDln1f = residApiService.getUsagerDln1f(initialDemandeDTO, residApiUrlV2, jwt, usagerId);

        if(usagerDln1f == null){
            LOGGER.info("RESID nous retourne null");
            DonneesExternesDemandeDTO donneesExternesDemandeDTO = new DonneesExternesDemandeDTO();
            donneesExternesDemandeDTO.setDemande(null);
            donneesExternesDemandeDTO.setStatut(DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.NOK);
            return mapper.valueToTree(donneesExternesDemandeDTO);
        }

        LOGGER.info("REPONSE API RESID: {}", usagerDln1f);

        DemandeDTO demandeDTO = new DemandeDTO();
        Object value = residInitialDemandeMapper.mapperDonneesResid(usagerDln1f, usagerId, initialDemandeDTO);
        demandeDTO.setContenu(mapper.valueToTree(value));

        DonneesExternesDemandeDTO donneesExternesDemandeDTO = new DonneesExternesDemandeDTO();
        donneesExternesDemandeDTO.setDemande(demandeDTO);
        donneesExternesDemandeDTO.setSource(SourceDonneesEnum.RESID.name());
        donneesExternesDemandeDTO.setStatut(DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.OK);

        return mapper.valueToTree(donneesExternesDemandeDTO);
    }

    private boolean isMconnectCall(Map<String, String[]> params) {
        if(params == null){
            return false;
        }
        return params.get(DONNEES_EXTERNES_MCONNECT_BIRTHDATE) != null || params.get(DONNEES_EXTERNES_MCONNECT_FAMILYNAME) != null
                || params.get(DONNEES_EXTERNES_MCONNECT_GIVENNAME) != null || params.get(DONNEES_EXTERNES_MCONNECT_BIRTHPLACECITY) != null
                || params.get(DONNEES_EXTERNES_MCONNECT_BIRTHPLACECOUNTRY) != null || params.get(DONNEES_EXTERNES_MCONNECT_BIRTHNAME) != null;
    }

}
