package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;
import mc.gouv.xaf.shared.dto.DonneesExternesDemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet mettant à disposition les donnees externes
 *
 * @author agaidi
 */
@Controller
@RequestMapping("/getInitialDemande")
public class InitialDemandeController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitialDemandeController.class);

    private static final String MCONNECT_PARAM_GIVENNAME = "GivenName";
    private static final String MCONNECT_PARAM_FAMILYNAME = "FamilyName";
    private static final String MCONNECT_PARAM_BIRTHDATE = "BirthDatetime";
    private static final String MCONNECT_PARAM_BIRTHNAME = "BirthName";
    private static final String MCONNECT_PARAM_BIRTHPLACE = "birthPlace";
    private static final String MCONNECT_PARAM_BIRTHCITY = "birthPlaceCity";
    private static final String MCONNECT_PARAM_BIRTHCOUNTRY = "birthPlaceCountry";
    private static final String USAGER_INFO_EMAIL = "usagerInfoEmail";
    private static final String USAGER_INFO_TITRE = "usagerInfoTitre";
    private static final String USAGER_INFO_NOM = "usagerInfoNom";
    private static final String USAGER_INFO_PRENOM = "usagerInfoPrenom";

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity doGet(HttpServletRequest request) {
        LOGGER.info("====================== /InitialDemandeServlet doGet()");

        // Vérification si l'usager est connecté
        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        ObjectMapper omapper = new ObjectMapper();
        omapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<PropertiesDTO> properties = getAfApiClient().getFrontProperties();
        PropertiesDTO property = properties.stream()
                .filter(prop -> "XAF_DONNEES_EXTERNES_PARAMETER_LIST".equals(prop.getKey())).findFirst().orElse(null);

        /*
         * liste des params propres aux TS, que le front peut utiliser pour faire une recherche de donnees externes. Ces
         * params sont forwardés à l'api lors de l'appel vers donneesExternes.
         */
        Map<String, String[]> data = new HashMap<>();

        if (property != null && property.getValue() != null) {
            try {
                List<String> parameters = omapper.readValue(property.getValue(), new TypeReference<List<String>>() {
                });
                parameters.forEach(
                        parameterName -> data.put(parameterName, request.getParameterMap().get(parameterName)));
            } catch (IOException e) {
                LOGGER.warn(
                        "Impossible de traiter XAF_DONNEES_EXTERNES_PARAMETER_LIST. Vérifier le format de la properties",
                        e);
            }
        }

        try {
            if (usagerInfosDTO.isMConnect()) {
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                DonneesExternesDTO donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                data.put(MCONNECT_PARAM_FAMILYNAME,
                        new String[]{donneesMConnectDTO.getMconnect().getFamilyName().toUpperCase()});
                data.put(MCONNECT_PARAM_GIVENNAME, new String[]{donneesMConnectDTO.getMconnect().getGivenName()});
                data.put(MCONNECT_PARAM_BIRTHDATE, new String[]{new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(donneesMConnectDTO.getMconnect().getBirthDatetime())});
                data.put(MCONNECT_PARAM_BIRTHNAME, new String[]{donneesMConnectDTO.getMconnect().getBirthName()});
                data.put(MCONNECT_PARAM_BIRTHPLACE, new String[]{donneesMConnectDTO.getMconnect().getBirthPlace()});
                data.put(MCONNECT_PARAM_BIRTHCITY,
                        new String[]{donneesMConnectDTO.getMconnect().getBirthPlaceCity()});
                data.put(MCONNECT_PARAM_BIRTHCOUNTRY,
                        new String[]{donneesMConnectDTO.getMconnect().getBirthPlaceCountry()});
            } else{
                data.put(USAGER_INFO_NOM, new String[]{usagerInfosDTO.getNom()});
                data.put(USAGER_INFO_PRENOM, new String[]{usagerInfosDTO.getPrenom()});
            }
            data.put(USAGER_INFO_EMAIL, new String[]{usagerInfosDTO.getEmail()});
            data.put(USAGER_INFO_TITRE, new String[]{String.valueOf(usagerInfosDTO.getTitre())});

            JsonNode retour = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

            DonneesExternesDemandeDTO resultSearch = mapper.treeToValue(retour, DonneesExternesDemandeDTO.class);

            if (resultSearch.getStatut() == DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.OK) {
                request.getSession().setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, resultSearch.getDemande());
                LOGGER.info("====================== Fin /InitialDemandeServlet doGet()");
                return ResponseEntity.ok(resultSearch.getDemande());
            }
            if (DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.OK.equals(resultSearch.getStatut())) {
                return ResponseEntity.ok().build();
            } else if (DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.CONFLICT
                    .equals(resultSearch.getStatut())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("JsonProcessingException. Impossible de recuperer les donnees externes", e);
            return ResponseEntity.internalServerError().build();
        } catch (IllegalArgumentException e) {
            LOGGER.error("IOException. Impossible de recuperer les donnees externes", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    public ResponseEntity doDelete(HttpServletRequest request) {
        LOGGER.info("====================== /demandeLock doDelete()");
        request.getSession().setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, null);
        LOGGER.info("====================== Fin /demandeLock doDelete()");
        return ResponseEntity.ok().build();
    }

}
