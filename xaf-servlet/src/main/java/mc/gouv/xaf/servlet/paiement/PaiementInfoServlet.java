package mc.gouv.xaf.servlet.paiement;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.apiclient.paiement.PaiementApiClient;
import mc.gouv.xaf.apiclient.paiement.PaiementConstant;
import mc.gouv.xaf.apiclient.paiement.monetico.dto.MoneticoDTO;
import mc.gouv.xaf.servlet.AbstractAfServlet;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.RequestConstant;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;

/**
 * Servlet permettant au Front de récupérer les données afin de générer le formulaire de paiement
 *
 * @author mboutelier.ext
 */
public class PaiementInfoServlet extends AbstractAfServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaiementInfoServlet.class);
    private static final long serialVersionUID = -9205530083929644719L;

    protected PaiementApiClient getStcApiClient() {
        return new PaiementApiClient(AfServletGouvPropertiesResolver.getApiUrl(),
                AfServletGouvPropertiesResolver.getApiJwt());
    }

    /**
     * Interface Aller
     */
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /paiement-info doGet()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        LOGGER.info("Récupération des paramètres...");
        String demandeIds = request.getParameter(RequestConstant.DEMANDES_ID_PARAM);

        String langue = request.getParameter(RequestConstant.LANGUE_PARAM);
        boolean iframe = Boolean.parseBoolean(request.getParameter(PaiementConstant.IFRAME_PARAM));


        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        String safeIds = demandeIds.replaceAll("[\n\r\t]", "_");
        LOGGER.info("Récupération des données de paiement pour la demande {}...", safeIds);
        MoneticoDTO paiement = getStcApiClient().getPaiement(demandeIds, langue, usagerId, iframe);

        response.setStatus(HttpStatus.SC_OK);
        ObjectMapper mapper = new ObjectMapper();

        try {
            String repJson = mapper.writeValueAsString(paiement);
            response.setContentType("application/json");
            IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        } catch (Exception e) {
            LOGGER.error("PaiementInfoServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /paiement-info doGet()\n");
    }
}
