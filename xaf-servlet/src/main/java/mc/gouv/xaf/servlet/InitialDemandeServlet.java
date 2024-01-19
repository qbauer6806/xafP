package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SessionConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;
import mc.gouv.xaf.shared.dto.DonneesExternesDemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * Servlet mettant à disposition les donnees externes
 *
 * @author agaidi
 */
public class InitialDemandeServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027084L;
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialDemandeServlet.class);

    private static final String MCONNECT_PARAM_GIVENNAME = "GivenName";
    private static final String MCONNECT_PARAM_FAMILYNAME = "FamilyName";
    private static final String MCONNECT_PARAM_BIRTHDATE = "BirthDatetime";
    private static final String MCONNECT_PARAM_BIRTHNAME = "BirthName";
    private static final String MCONNECT_PARAM_BIRTHPLACE = "birthPlace";
    private static final String MCONNECT_PARAM_BIRTHCITY = "birthPlaceCity";
    private static final String MCONNECT_PARAM_BIRTHCOUNTRY = "birthPlaceCountry";
    private static final String USAGER_INFO_EMAIL = "usagerInfoEmail";
    private static final String USAGER_INFO_TITRE = "usagerInfoTitre";

    /**
     * Vérifie si l'utilisateur est autorisé à faire la requête et prépare les objets communs aux requêtes :<br>
     * <ol>
     * <li>Un UsagerInfosDTO contenant les infos de l'usager.</li>
     * <li>L'id de la demande déjà parsé en entier (si présent)</li>
     * <li>Flag indiquant la présence de demandes complémentaires</li>
     * <li>L'id de la demande d'information complémentaire déjà parsé en entier (si présent)</li>
     * </ol>
     */
    private Object[] setup(HttpServletRequest request, HttpServletResponse response) {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return new Object[0];
        }

        String sDemandeId = request.getParameter("demandeId");
        Integer demandeId = sDemandeId != null ? Integer.parseInt(sDemandeId) : null;

        return new Object[] { usagerInfosDTO, demandeId };
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /InitialDemandeServlet doGet()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
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
            if (usagerInfosDTO.ismConnect()) {
                DonneesExternesDTO donneesMConnectDTO;
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                data.put("usagerId", new String[] { usagerInfosDTO.getId() + "" });
                data.put(MCONNECT_PARAM_FAMILYNAME,
                        new String[] { donneesMConnectDTO.getMconnect().getFamilyName().toUpperCase() });
                data.put(MCONNECT_PARAM_GIVENNAME, new String[] { donneesMConnectDTO.getMconnect().getGivenName() });
                data.put(MCONNECT_PARAM_BIRTHDATE, new String[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(donneesMConnectDTO.getMconnect().getBirthDatetime()) });
                data.put(MCONNECT_PARAM_BIRTHNAME, new String[] { donneesMConnectDTO.getMconnect().getBirthName() });
                data.put(MCONNECT_PARAM_BIRTHPLACE, new String[] { donneesMConnectDTO.getMconnect().getBirthPlace() });
                data.put(MCONNECT_PARAM_BIRTHCITY,
                        new String[] { donneesMConnectDTO.getMconnect().getBirthPlaceCity() });
                data.put(MCONNECT_PARAM_BIRTHCOUNTRY,
                        new String[] { donneesMConnectDTO.getMconnect().getBirthPlaceCountry() });
                data.put(USAGER_INFO_EMAIL,
                        new String[] { usagerInfosDTO.getEmail() });
                data.put(USAGER_INFO_TITRE, new String[] { String.valueOf(usagerInfosDTO.getTitre()) });

            }

            JsonNode retour = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

            DonneesExternesDemandeDTO resultSearch = mapper.treeToValue(retour, DonneesExternesDemandeDTO.class);

            response.setContentType(MediaType.APPLICATION_JSON);
            if (resultSearch.getStatut() == DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.OK) {
                mapper.writeValue(response.getOutputStream(), resultSearch.getDemande());
                request.getSession().setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, resultSearch.getDemande());
            }
            if (DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.OK.equals(resultSearch.getStatut())) {
                response.setStatus(HttpStatus.SC_OK);
            } else if (DonneesExternesDemandeDTO.DonneesExternesStatutRetourEnum.CONFLICT
                    .equals(resultSearch.getStatut())) {
                response.setStatus(HttpStatus.SC_CONFLICT);
            } else {
                response.setStatus(HttpStatus.SC_NOT_FOUND);
            }

            response.getOutputStream().flush();
        } catch (JsonProcessingException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("JsonProcessingException. Impossible de recuperer les donnees externes", e);
        } catch (IOException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            LOGGER.error("IOException. Impossible de recuperer les donnees externes", e);
        }

        LOGGER.info("====================== Fin /InitialDemandeServlet doGet()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandeLock doDelete()");
        request.getSession().setAttribute(SessionConstant.SESSION_DEMANDE_INITIALE, null);
        LOGGER.info("====================== Fin /demandeLock doDelete()");
    }

}
