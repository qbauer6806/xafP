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
    private static final String MCONNECT_PARAM_GIVENNAME = "GivenName";
    private static final String MCONNECT_PARAM_FAMILYNAME = "FamilyName";
    private static final String MCONNECT_PARAM_BIRTHDATE = "BirthDatetime";
    private static final String ULIS_PARAM_CONTRAT = "numerocontrat";
    private static final String ULIS_PARAM_FACTURE = "numerocontrat";
    private static final String ULIS_PARAM_TIERS = "numerotiers";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialDemandeServlet.class);

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
        Map<String, String[]> data = new HashMap<>();
        /* liste des params propres aux TS, que le front peut utiliser pour faire une recherche de donnees externes */

        PropertiesDTO property = properties.stream()
                .filter(prop -> "XAF_DONNEES_EXTERNES_PARAMETER_LIST".equals(prop.getKey())).findFirst().orElse(null);

        if (property != null) {
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

        DonneesExternesDTO donneesMConnectDTO;

        try {

            if (usagerInfosDTO.ismConnect()) {
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                data.put("usagerId", new String[] { usagerInfosDTO.getId() + "" });
                data.put(MCONNECT_PARAM_FAMILYNAME, new String[] { donneesMConnectDTO.getMconnect().getFamilyName() });
                data.put(MCONNECT_PARAM_GIVENNAME, new String[] { donneesMConnectDTO.getMconnect().getGivenName() });
                data.put(MCONNECT_PARAM_BIRTHDATE, new String[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                        .format(donneesMConnectDTO.getMconnect().getBirthDatetime()) });
            }

            JsonNode retour = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

            DonneesExternesDemandeDTO resultSearch = mapper.treeToValue(retour, DonneesExternesDemandeDTO.class);

            response.setContentType(MediaType.APPLICATION_JSON);
            if (resultSearch.getStatut() == 200) {
                mapper.writeValue(response.getOutputStream(), resultSearch.getDemande());
                /* TODO: check si on stocke l'objet en session de façon à le récupérer à la creation de la demande */
                request.getSession().setAttribute(SessionConstant.SESSION_INITIAL_DEMANDE, resultSearch.getDemande());
            }
            response.setStatus(resultSearch.getStatut());
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

        LOGGER.info("====================== Fin /demandeLock doDelete()");
    }

}
