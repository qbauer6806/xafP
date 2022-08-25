package mc.gouv.xaf.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import mc.gouv.xaf.servlet.enums.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeInputDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;

/**
 * 
 * Servlet mettant à disposition le service /demandes avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspondants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class DemandesServlet extends AbstractAfServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(DemandesServlet.class);

    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response,
            HttpMethod httpMethod) throws UnsupportedOperationException, IOException {

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
        }

        String pathInfo = request.getPathInfo();
        String demandeId = null;
        boolean demandeInfosCompl = false;
        Integer demandeInfosComplId = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            String[] pathElems = pathInfo.split("/");
            demandeId = pathElems[1];
            // Gérer le cas des demandes d'informations complémentaires par rapport à une demande
            // Et le cas des affectations à une demande
            if (pathElems.length > 2) {
                if (pathElems[2].equals("complements")) {
                    demandeInfosCompl = true;
                    if (pathElems.length > 3) {
                        demandeInfosComplId = Integer.valueOf(pathElems[3]);
                    }
                } else {
                    // Opération interdite (exemple /statuts ou /affectations, auxquelles le FRONT ne doit pas avoir accès)
                    return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN,
                            "Erreur: opération interdite");
                }
            }
        }

        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();

        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);

        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId + ", DemandeID=" + demandeId
                + ", DemandeCompl?=" + demandeInfosCompl + ", DemandeComplID=" + demandeInfosComplId);

        AfApiClient afApiClient = getAfApiClient();
        ObjectMapper mapper = new ObjectMapper();

        String repJson = null;
        if (HttpMethod.PUT.equals(httpMethod) || HttpMethod.POST.equals(httpMethod)) {
            // Récupération du JSON reçu en input et transmission au 2ème service en
            // UTF8
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.toString().length() == 0) {
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                        "Erreur: JSON manquant");
            }

            if (demandeInfosCompl) {
                LOGGER.info("Appel à la démarche pour répondre à la demande d'informations complémentaires");
                DemandeComplementsReponseDTO reponse = mapper.readValue(buffer.toString(),
                        DemandeComplementsReponseDTO.class);
                reponse.setAgentId(null);
                reponse.setUsagerId(usagerId);
                DemandeComplementsDTO demandeComplement = afApiClient
                        .repondreDemandeComplements(Integer.parseInt(demandeId), demandeInfosComplId, reponse);

                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_OK);
                repJson = mapper.writeValueAsString(demandeComplement);
            } else {
                LOGGER.info("Appel à la démarche pour créer la demande");
                DemandeInputDTO demandeInput = mapper.readValue(buffer.toString(), DemandeInputDTO.class);
                // Ajout des données externes MConnect si elles sont présentes (afin que l'API puisse les prendre en compte pour les places dans les bons endroits
                // du contenu de la demande. Ceci afin d'éviter un potentiel "hack" de la part de l'usager sur le FO)
                if (usagerInfosDTO != null && usagerInfosDTO.getDonneesExternes() != null && usagerInfosDTO.getDonneesExternes().getMconnect() != null) {
                	demandeInput.setDonneesMConnect(usagerInfosDTO.getDonneesExternes().getMconnect());
                }
                DemandeDTO demandeDto = afApiClient.creerDemande(demandeInput, usagerId);

                // TODO : gestion des erreurs
                response.setStatus(HttpStatus.SC_CREATED);
                repJson = mapper.writeValueAsString(demandeDto);
            }
        } else if (HttpMethod.GET.equals(httpMethod)) {
            if (!demandeInfosCompl) {
                if (demandeId != null) {
                    LOGGER.info("Appel à la démarche pour récupérer la demande");
                    DemandeDTO demandeDto = afApiClient.getDemande(usagerId, Integer.parseInt(demandeId));
                    // TODO : gestion des erreurs
                    response.setStatus(HttpStatus.SC_OK);
                    repJson = mapper.writeValueAsString(demandeDto);
                } else {
                    LOGGER.info("Appel à la démarche pour récupérer toutes les demandes");
                    List<DemandeDTO> demandeDtos = afApiClient.getDemandes(usagerId);
                    // TODO : gestion des erreurs
                    response.setStatus(HttpStatus.SC_OK);
                    repJson = mapper.writeValueAsString(demandeDtos);
                }
            } else {
                if (demandeInfosComplId != null) {
                    LOGGER.info("Appel à la démarche pour récupérer la demande d'informations complémentaires");
                    DemandeComplementsDTO demandeComplementsDto = afApiClient
                            .getDemandeComplements(Integer.parseInt(demandeId), demandeInfosComplId);
                    // TODO : gestion des erreurs
                    response.setStatus(HttpStatus.SC_OK);
                    repJson = mapper.writeValueAsString(demandeComplementsDto);
                } else {
                    LOGGER.info(
                            "Appel à la démarche pour récupérer toutes les demandes d'informations complémentaires");
                    List<DemandeComplementsDTO> demandeComplementsDtos = afApiClient
                            .getDemandesComplements(Integer.parseInt(demandeId));
                    // TODO : gestion des erreurs
                    response.setStatus(HttpStatus.SC_OK);
                    repJson = mapper.writeValueAsString(demandeComplementsDtos);
                }
            }
        }

        response.setContentType("application/json");
        if (repJson != null) {
        	IOUtils.copy(new ByteArrayInputStream(repJson.getBytes()), response.getOutputStream());
        }

        return response;
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doPost()");

        try {
            doHttpMethod(request, response, HttpMethod.POST);
        } catch (IOException e) {
            LOGGER.error("DemandesServlet - Une erreur est survenue lors de l'appel à la méthode POST", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /demandes doPost()");
    }

    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doPut()");

        try {
            doHttpMethod(request, response, HttpMethod.PUT);
        } catch (IOException e) {
            LOGGER.error("DemandesServlet - Une erreur est survenue lors de l'appel à la méthode PUT", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /demandes doPut()");
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /demandes doGet()");

        try {
            doHttpMethod(request, response, HttpMethod.GET);
        } catch (Exception e) {
            LOGGER.error("DemandesServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        LOGGER.info("====================== Fin /demandes doGet()");
    }
}
