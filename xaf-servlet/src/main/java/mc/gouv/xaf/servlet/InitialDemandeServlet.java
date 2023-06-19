package mc.gouv.xaf.servlet;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DonneesExternesDTO;
import mc.gouv.xaf.shared.dto.DonneesExternesDemandeDTO;

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

        if (usagerInfosDTO.ismConnect()) {

            DonneesExternesDTO donneesMConnectDTO;

            Map<String, String[]> data = new HashMap<>();

            if (usagerInfosDTO.ismConnect()) {
                JsonNode usagerJson = usagerInfosDTO.getDonneesExternes();
                ObjectMapper omapper = new ObjectMapper();
                omapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                data.put("numerocontrat", request.getParameterMap().get("numerocontrat"));
                data.put("numerotiers", request.getParameterMap().get("numerotiers"));
                data.put("numerofacture", request.getParameterMap().get("numerofacture"));
                data.put("usagerId", new String[] { usagerInfosDTO.getId() + "" });
                try {
                    donneesMConnectDTO = omapper.treeToValue(usagerJson, DonneesExternesDTO.class);
                    data.put(MCONNECT_PARAM_FAMILYNAME,
                            new String[] { donneesMConnectDTO.getMconnect().getFamilyName() });
                    data.put(MCONNECT_PARAM_GIVENNAME,
                            new String[] { donneesMConnectDTO.getMconnect().getGivenName() });
                    data.put(MCONNECT_PARAM_BIRTHDATE, new String[] { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
                            .format(donneesMConnectDTO.getMconnect().getBirthDatetime()) });

                    JsonNode retour = getAfApiClient().getDonneesExternes(usagerInfosDTO.getId(), data);
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                    mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));

                    DonneesExternesDemandeDTO resultSearch = mapper.treeToValue(retour,
                            DonneesExternesDemandeDTO.class);

                    response.setContentType(MediaType.APPLICATION_JSON);
                    if (resultSearch.getStatut() == 200)
                        mapper.writeValue(response.getOutputStream(), resultSearch.getDemande());
                    response.setStatus(resultSearch.getStatut());
                    response.getOutputStream().flush();
                } catch (JsonProcessingException e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    LOGGER.error("JsonProcessingException. Impossible de recuperer les donnees externes", e);
                } catch (IOException e) {
                    response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                    LOGGER.error("IOException. Impossible de recuperer les donnees externes", e);
                }

            }

        }

        LOGGER.info("====================== Fin /InitialDemandeServlet doGet()");
    }

    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandeLock doDelete()");

        LOGGER.info("====================== Fin /demandeLock doDelete()");
    }

}
