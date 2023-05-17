package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.enums.HttpMethod;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Servlet mettant à disposition le service /demandes avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspondants dans le back-end générique.
 *
 * @author qdeme
 */
public class DemandesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesServlet.class);

    /**
     * Vérifie si l'utilisateur est autorisé à faire la requête et prépare les objets communs aux requêtes :<br>
     * <ol>
     *     <li>Un UsagerInfosDTO contenant les infos de l'usager.</li>
     *     <li>L'id de la demande déjà parsé en entier (si présent)</li>
     *     <li>Flag indiquant la présence de demandes complémentaires</li>
     *     <li>L'id de la demande d'information complémentaire déjà parsé en entier (si présent)</li>
     * </ol>
     */
    private Object[] setup(HttpServletRequest request, HttpServletResponse response) {
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
            return new Object[0];
        }

        String pathInfo = request.getPathInfo();
        Integer demandeId = null;
        boolean demandeInfosCompl = false;
        Integer demandeInfosComplId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            demandeId = Integer.valueOf(pathElems[1]);
            // Gérer le cas des demandes d'informations complémentaires par rapport à une demande
            // Et le cas des affectations à une demande
            if (pathElems.length > 2) {
                if (!StringUtils.equals(pathElems[2], RequestConstant.COMPLEMENTS_PATH)) {
                    // Opération interdite (exemple /statuts ou /affectations, auxquelles le FRONT ne doit pas avoir accès)
                    AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN,
                            "Erreur: opération interdite");
                    return new Object[0];
                }
                demandeInfosCompl = true;
                if (pathElems.length > 3) {
                    demandeInfosComplId = Integer.valueOf(pathElems[3]);
                }
            }
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

        LOGGER.info("DemarcheID={}, UsagerID={}, DemandeID={}, DemandeCompl?={}, DemandeComplID={}",
                demarcheId, usagerId, demandeId, demandeInfosCompl, demandeInfosComplId);
        return new Object[]{usagerInfosDTO, demandeId, demandeInfosCompl, demandeInfosComplId};
    }

    /**
     * Factorisation des méthodes PUT et POST
     *
     * @param request    Requête initiale de la Servlet
     * @param response   Réponse initiale de la Servlet
     * @param httpMethod Indique si l'on souhaite effectuer un POST ou un PUT
     */
    public void doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) {

        if (!HttpMethod.PUT.equals(httpMethod) && !HttpMethod.POST.equals(httpMethod)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Mauvais appel de méthode.");
            return;
        }

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        UsagerInfosDTO usagerInfosDTO = (UsagerInfosDTO) params[0];
        Integer demandeId = (Integer) params[1];
        boolean demandeInfosCompl = (boolean) params[2];
        Integer demandeInfosComplId = (Integer) params[3];
        AfApiClient afApiClient = getAfApiClient();

        try {
            // Récupération du JSON reçu en input et transmission au 2ème service en UTF8
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (buffer.toString().length() == 0) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Erreur: JSON manquant");
                return;
            }

            String repJson;
            ObjectMapper mapper = new ObjectMapper();
            if (demandeInfosCompl) {
                LOGGER.info("Appel à la démarche pour répondre à la demande d'informations complémentaires");
                DemandeComplementsReponseDTO reponse = mapper.readValue(buffer.toString(), DemandeComplementsReponseDTO.class);
                reponse.setAgentId(null);
                reponse.setUsagerId(usagerInfosDTO.getId());
                DemandeComplementsDTO demandeComplement = afApiClient.repondreDemandeComplements(demandeId, demandeInfosComplId, reponse);

                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_OK);
                repJson = mapper.writeValueAsString(demandeComplement);
            } else {
                DemandeInputDTO demandeInput = mapper.readValue(buffer.toString(), DemandeInputDTO.class);
                // Ajout des données externes MConnect si elles sont présentes (afin que l'API puisse les prendre en compte pour les places dans les bons endroits
                // du contenu de la demande. Ceci afin d'éviter un potentiel "hack" de la part de l'usager sur le FO)
                if (usagerInfosDTO.getDonneesExternes() != null && usagerInfosDTO.getDonneesExternes().get("mconnect") != null) {
                    demandeInput.setDonneesMConnect(mapper.treeToValue(usagerInfosDTO.getDonneesExternes().get("mconnect"), DonneesMConnectDTO.class));
                }
                
                DemandeDTO demandeDto;
            	if (HttpMethod.POST.equals(httpMethod)) {
	                LOGGER.info("Appel à la démarche pour créer la demande");
	                demandeDto = afApiClient.creerDemande(demandeInput, usagerInfosDTO.getId());
            	} else {
	                LOGGER.info("Appel à la démarche pour mettre à jour la demande {}", demandeId);
	                demandeDto = afApiClient.updateDemande(demandeId, demandeInput, usagerInfosDTO.getId());
            	}
            	
                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_CREATED);
                repJson = mapper.writeValueAsString(demandeDto);
            }

            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }
        } catch (IOException e) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "DemandesServlet - Une erreur est survenue lors de l'appel à la méthode " + httpMethod.name());
        }
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doPost()");
        doHttpMethod(request, response, HttpMethod.POST);
        LOGGER.info("====================== Fin /demandes doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doPut()");
        doHttpMethod(request, response, HttpMethod.PUT);
        LOGGER.info("====================== Fin /demandes doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doGet()");

        Object[] params = setup(request, response);
        if (params.length == 0) {
            return;
        }
        Integer usagerId = ((UsagerInfosDTO) params[0]).getId();
        Integer demandeId = (Integer) params[1];
        boolean demandeInfosCompl = (boolean) params[2];
        Integer demandeInfosComplId = (Integer) params[3];

        try {
            String repJson;
            AfApiClient afApiClient = getAfApiClient();
            ObjectMapper mapper = new ObjectMapper();
            if (!demandeInfosCompl) {
                if (demandeId != null) {
                    LOGGER.info("Appel à la démarche pour récupérer la demande");
                    DemandeDTO demandeDto = afApiClient.getDemande(usagerId, demandeId);
                    // TODO : gestion des erreurs
                    repJson = mapper.writeValueAsString(demandeDto);
                } else {
                    LOGGER.info("Appel à la démarche pour récupérer toutes les demandes");
                    List<DemandeDTO> demandeDtos = afApiClient.getDemandes(usagerId);
                    // TODO : gestion des erreurs
                    repJson = mapper.writeValueAsString(demandeDtos);
                }
            } else {
                if (demandeInfosComplId != null) {
                    LOGGER.info("Appel à la démarche pour récupérer la demande d'informations complémentaires");
                    DemandeComplementsDTO demandeComplementsDto = afApiClient.getDemandeComplements(demandeId, demandeInfosComplId);
                    // TODO : gestion des erreurs
                    repJson = mapper.writeValueAsString(demandeComplementsDto);
                } else {
                    LOGGER.info(
                            "Appel à la démarche pour récupérer toutes les demandes d'informations complémentaires");
                    List<DemandeComplementsDTO> demandeComplementsDtos = afApiClient.getDemandesComplements(demandeId);
                    // TODO : gestion des erreurs
                    repJson = mapper.writeValueAsString(demandeComplementsDtos);
                }
            }
            response.setContentType(MediaType.APPLICATION_JSON);
            if (repJson != null) {
                IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
            }
        } catch (IOException e) {
            LOGGER.error("DemandesServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        response.setStatus(HttpStatus.SC_OK);
        LOGGER.info("====================== Fin /demandes doGet()");
    }
}
