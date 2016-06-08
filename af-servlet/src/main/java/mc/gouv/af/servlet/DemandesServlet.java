package mc.gouv.af.servlet;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import mc.gouv.af.servlet.dto.UsagerInfosDTO;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;
import mc.gouv.af.servlet.util.AppFactoryServletUtils.ServiceTarget;
import mc.gouv.demarches.api.model.DemandeComplementsReponseDTO;

/**
 * Servlet mettant à disposition le service /demandes avec les méthodes PUT, POST, GET, DELETE.
 * Cette servlet récupère le DemarcheID ainsi que l'UsagerID (depuis la session) et appelle les WS
 * correspontants dans le back-end générique.
 * 
 * @author qdeme
 *
 */
public class DemandesServlet extends HttpServlet {

    private static final long serialVersionUID = -7898768899143027088L;

    private static Logger LOGGER = LoggerFactory.getLogger(DemandesServlet.class);
    
    private enum HttpMethod {
        PUT,
        POST,
        GET,
        DELETE;
    }
    
    public HttpServletResponse doHttpMethod(HttpServletRequest request, HttpServletResponse response, HttpMethod httpMethod) throws UnsupportedOperationException, IOException {
        
        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED, "Utilisateur non autorisé");
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
                }
                else {
                    // Opération interdite (exemple /statuts ou /affectations, auxquelles le FRONT ne doit pas avoir accès)
                    return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN, "Erreur: opération interdite");
                }
            }
        }
        
        // Récupération de l'ID de l'usager
        Integer usagerId = usagerInfosDTO.getId();
        
        // Récupération de l'ID de la démarche dans le Context-Param
        String demarcheId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
        
        LOGGER.info("DemarcheID=" + demarcheId + ", UsagerID=" + usagerId + ", DemandeID=" + demandeId + ", DemandeCompl?=" + demandeInfosCompl + ", DemandeComplID=" + demandeInfosComplId);
        
        // Transmission du JSON d'infos du compte démarche, reçu en input, dans le WS back-end
        
        // GET /demandes/{demandeId} => retourne la demande spécifiée
        // GET /demandes => retourne toutes les demandes de l'usager connecté
        // POST /demandes => crée une demande pour cette démarche et cet usager
        // POST /demandes/{demandeId} => pour modifier une demande
        // DELETE /demandes/{demandeId} => pour supprimer une demande
        
        // Création du client HTTP avec la bonne adresse
        HttpClient httpClient = HttpClientBuilder.create().
                setDefaultCredentialsProvider(AppFactoryServletUtils.getCredentialsProvider(ServiceTarget.DEMARCHES)).build();
        HttpRequestBase finalRequest = null;
        String url = AppFactoryServletUtils.DEMANDES_URL;
        if (HttpMethod.GET.equals(httpMethod) || HttpMethod.POST.equals(httpMethod) || (HttpMethod.PUT.equals(httpMethod) && demandeInfosCompl)) {
            if (!demandeInfosCompl) {
                // Demande
                if (demandeId != null) {
                    // Modification
                    url += "/" + demarcheId + "/" + demandeId;
                }
                else {
                    // Création
                    url += "/" + demarcheId + "?usagerId=" + usagerId;
                }
            }
            else {
                // Demande d'informations complémentaires
                if (demandeInfosComplId != null) {
                    // Modification
                    url += "/" + demarcheId + "/" + demandeId + "/complements/" + demandeInfosComplId;
                }
                else {
                    // Création
                    url += "/" + demarcheId + "/" + demandeId + "/complements";
                }
            }
            if (HttpMethod.PUT.equals(httpMethod)) {
                finalRequest = new HttpPut(url);
            }
            else if (HttpMethod.GET.equals(httpMethod)) {
                finalRequest = new HttpGet(url);
            }
            else {
                if (demandeInfosCompl) {
                    // Demande d'informations complémentaires en POST = création de demande d'IC : interdit pour le FRONT
                    return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN, "Erreur: opération interdite");
                }
                finalRequest = new HttpPost(url);
            }
        }
        else if (HttpMethod.DELETE.equals(httpMethod)) {
            if (demandeId == null) {
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "demandeId manquant");
            }
            if (!demandeInfosCompl) {
                // Demande
                url += "/" + demarcheId + "/" + demandeId;
            }
            else {
                // Suppression d'une demande d'informations complémentaires : interdit pour le FRONT
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN, "Erreur: opération interdite");
            }
            finalRequest = new HttpDelete(url);
        }
        
        if (HttpMethod.POST.equals(httpMethod) || (HttpMethod.PUT.equals(httpMethod) && demandeInfosCompl)) {
            // Soit POST pour demande et demande d'IC, soit PUT pour demande d'IC uniquement
            finalRequest.setHeader("Content-Type", "application/json; charset=UTF-8");
            
            // Récupération du JSON reçu en input et transmission au 2ème service en UTF8
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            
            if (buffer.toString().length() == 0) {
                return AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST, "Erreur: JSON manquant");
            }
            
            StringEntity input = null;
            
            // Étape importante pour le PUT (répondre à une demande d'informations complémentaires)
            // L'usagerId est à remplir par AppFactoryServlet.
            // Il faut donc analyser le JSON fourni afin de renseigner ce champ et de le réinjecter dans l'appel à Demarches
            // Et également supprimer un éventuel "usagerId" ou "agentId" que le client aurait déjà mis... (il faut l'empêcher)
            if (HttpMethod.PUT.equals(httpMethod) && demandeInfosCompl) {
                ObjectMapper mapper = new ObjectMapper();
                DemandeComplementsReponseDTO reponse = mapper.readValue(buffer.toString(), DemandeComplementsReponseDTO.class);
                reponse.setAgentId(null);
                reponse.setUsagerId(usagerId.toString());
                String reponseStr = mapper.writeValueAsString(reponse);
                input = new StringEntity(reponseStr,"UTF-8");
            }
            else {
                input = new StringEntity(buffer.toString(),"UTF-8");
            }
            
            ((HttpEntityEnclosingRequestBase)finalRequest).setEntity(input);
        }
        
        // Envoi de la requête
        LOGGER.info("Appel du WS Demarches: " + url);
        HttpResponse finalResponse = httpClient.execute(finalRequest);
        LOGGER.info("Code réponse : " + finalResponse.getStatusLine().getStatusCode());
        
        // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
        LOGGER.info("Constitution de la réponse pour retour au client");
        response.setContentType("application/json");
        
        response.setStatus(finalResponse.getStatusLine().getStatusCode());
        
        IOUtils.copy(finalResponse.getEntity().getContent(), response.getOutputStream());
        
        return response;
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /demandes doPost()");
        
        response = doHttpMethod(request, response, HttpMethod.POST);
        
        LOGGER.info("====================== Fin /demandes doPost()");
    }
    
    @Override
    public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /demandes doPut()");
        
        response = doHttpMethod(request, response, HttpMethod.PUT);
        
        LOGGER.info("====================== Fin /demandes doPut()");
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /demandes doGet()");
        
        response = doHttpMethod(request, response, HttpMethod.GET);
        
        LOGGER.info("====================== Fin /demandes doGet()");
    }
    
    @Override
    public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LOGGER.info("====================== /demandes doDelete()");
        
        response = doHttpMethod(request, response, HttpMethod.DELETE);
        
        LOGGER.info("====================== Fin /demandes doDelete()");
    }
}
