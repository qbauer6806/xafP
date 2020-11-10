package mc.gouv.xaf.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import mc.gouv.vscan.shared.dto.ScanDTO;
import mc.gouv.vscan.shared.dto.ScanRequestDTO;
import mc.gouv.xaf.servlet.dto.FileUploadCompteurDTO;
import mc.gouv.xaf.servlet.dto.FileUploadResponseDTO;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils.ServiceTarget;
import mc.gouv.xaf.shared.dto.AccessDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@MultipartConfig
public class FileUploadServlet extends AbstractAfServlet {

    private static final long serialVersionUID = 484237515919955392L;
    private static Logger LOGGER = LoggerFactory.getLogger(FileUploadServlet.class);

    private static final String EXTENSIONS_WHITELIST = "EXTENSIONS_WHITELIST";
    private static final String MAX_TAILLE_FICHIER = "MAX_TAILLE_FICHIER";
    private static final String VSCAN_ACTIVATION = "VSCAN_ACTIVATION";

    // Enregistre l'historique d'upload par session
    private final static Map<HttpSession, FileUploadCompteurDTO> usagersFileUploadCompteurs = new HashMap<>();

    // Compteur permettant de trigger un refresh des sessions et supprimer celles qui ne sont plus utilisées
    private static int compteurCleanSessions;

    @SuppressWarnings("deprecation")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.info("====================== /fileupload doPost()");

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(request);
        if (usagerInfosDTO == null) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_UNAUTHORIZED,
                    "Utilisateur non autorisé");
            return;
        }

        // Vérification du nombre de fichier uploadés sur la demande
        LOGGER.info("Vérification du nombre de fichiers déjà uploadés...");
        HttpSession session = request.getSession();
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload != null) {
            Duration duration = Duration.between(compteurUpload.getDatePremierUpload(), LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(AfServletGouvPropertiesResolver.getTempsIntervalleUpload());
            int maxUploadParIntervalle = Integer.parseInt(AfServletGouvPropertiesResolver.getMaxUploadParIntervalle());

            if (compteurUpload.getCompteur() >= maxUploadParIntervalle && duration.toMillis() < tempsParIntervalle) {
                LOGGER.info("La limite de nombre de fichiers uploadés a été atteinte");
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_METHOD_NOT_ALLOWED,
                        "Erreur: La limite de nombre de fichiers uploadés a été atteinte");
                return;
            }
            else if (duration.toMillis() > tempsParIntervalle) {
                // Supprimer le compteur en cas de dépassement
                usagersFileUploadCompteurs.remove(session);
            }
        }

        // Récupération du nom du fichier à envoyer
        String pathInfo = request.getPathInfo();
        String filename = null;
        if (pathInfo != null && pathInfo.length() > 1) {
            filename = pathInfo.split("/")[1];
        }

        if (StringUtils.isBlank(filename)) {
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                    "Erreur: nom du fichier manquant");
            return;
        }

        // ---  Vérification de la conformité du fichier
        // Vérification du type du fichier
        LOGGER.info("Vérification du type...");
        if (!estExtensionDansWhitelist(filename)) {
            LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({}), pas d'upload dans FILE", getExtensionsWhitelist());
            AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN,
                    "Erreur: le type/extension du fichier soumis n'est pas valide");
            return;
        }

        try {
            ObjectMapper mapper = new ObjectMapper();

            LOGGER.info("Vérification de la taille...");
            // Vérification de la taille du fichier
            Part part0 = request.getParts().iterator().next();
            PropertiesDTO propMaxTailleFichiers = getPropriete(MAX_TAILLE_FICHIER);
            PropertiesDTO propActivationVscan = getPropriete(VSCAN_ACTIVATION);
            if (propMaxTailleFichiers == null || propActivationVscan == null) {
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Une propriété obligatoire semble ne pas être définie");
                return;
            }
            int tailleMaxFichier = Integer.parseInt(propMaxTailleFichiers.getValue());
            // transformation B en MB
            int tailleMaxFichierMB = tailleMaxFichier * 1000000;
            if (part0.getSize() > tailleMaxFichierMB) {
                LOGGER.info("La taille du fichier depasse la taille max definie dans les propriétés ({})", tailleMaxFichier);
                AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_FORBIDDEN,
                        "Erreur: la taille du fichier depasse la taille max definie dans les propriétés");
                return;
            }

            // Appel à VSCAN afin d'effectuer le scan antivirus
            // Constitution de la requête
            boolean activationVscan = Boolean.parseBoolean(propActivationVscan.getValue());
            LOGGER.info("Activation de VSCAN: " + activationVscan);

            if (activationVscan) {
                LOGGER.info("Appel à VSCAN...");

                String urlVscan = AfServletGouvPropertiesResolver.getVscanUrl();
                LOGGER.info("URL = " + urlVscan);
                HttpClient clientVscan = HttpClientBuilder.create().build();
                MultipartEntityBuilder builderVscan = MultipartEntityBuilder.create();
                builderVscan.addPart("file", new InputStreamBody(part0.getInputStream(), part0.getContentType(), part0.getSubmittedFileName()));

                // Pour tester avec un fichier vérolé (EICAR)
                //builderVscan.addPart("file", new InputStreamBody(new ByteArrayInputStream("X5O!P%@AP[4\\PZX54(P^)7CC)7}$EICAR-STANDARD-ANTIVIRUS-TEST-FILE!$H+H*".getBytes()), "blason.jpg"));

                ScanRequestDTO scanRequest = new ScanRequestDTO();
                scanRequest.setCodeAppli(getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY));
                scanRequest.setFilename(filename);
                //scanRequest.setEnduserIpAddress(request.getRemoteAddr());
                scanRequest.setEnduserAppModule(getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY).toLowerCase() + "-frontserver");
                //scanRequest.setEnduserDenomination("Usager " + usagerInfosDTO.getId() + " (" + usagerInfosDTO.getLogin() + ")");

                String scanRequestStr = mapper.writeValueAsString(scanRequest);
                builderVscan.addPart("scanRequest", new StringBody(scanRequestStr));
                HttpEntity multipartVscan = builderVscan.build();
                HttpPost postRequestVscan = new HttpPost(urlVscan.toString());
                postRequestVscan.setEntity(multipartVscan);
                postRequestVscan.addHeader("Authorization", "Bearer " + AfServletGouvPropertiesResolver.getVscanJwt());
                HttpResponse postResponseVscan = clientVscan.execute(postRequestVscan);
                String vscanResp = IOUtils.toString(postResponseVscan.getEntity().getContent());
                LOGGER.info("VSCAN Response : " + postResponseVscan.getStatusLine() + "(" + vscanResp + ")");

                ScanDTO scanDto = mapper.readValue(vscanResp, ScanDTO.class);

                if (!scanDto.isResult()) {
                    LOGGER.info("VSCAN a détecté le fichier comme vérolé, fin du traitement, pas d'upload dans FILE");
                    response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_BAD_REQUEST,
                            "Erreur: le fichier soumis semble corrompu");
                    return;
                }

                LOGGER.info("VSCAN n'a pas considéré le fichier soumis comme vérolé");
            }

            // Génération de l'UUID
            UUID uuid = AppFactoryServletUtils.generateUUID();
            LOGGER.debug("UUID généré : {}", uuid.toString());

            String accountId = getServletContext().getInitParameter(AppFactoryServletUtils.DEMARCHEID_KEY);
            String containerId = getServletContext().getInitParameter(AppFactoryServletUtils.CONTAINER_KEY);

            LOGGER.debug("accountId = {}, containerId = {}", accountId, containerId);

            // Récupération de l'AccessID via appel WS à Demarches
            LOGGER.info("Appel à la démarche pour récupérer l'AccessID correspondant..");

            //Integer accessId = AppFactoryServletUtils.getAccessID(demarcheId, usagerInfosDTO.getId());
            AccessDTO access = getAfApiClient().getAccess(usagerInfosDTO.getId());
            Integer accessId = access.getPkAccess();
            
            LOGGER.debug("AccessID = {}", accessId);

            if (accessId == null) {
                response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_NOT_FOUND,
                        "Erreur: impossible de récupérer l'accès");
                return;
            }

            // Constitution du chemin virtuel du fichier
            // /appfactory/demarcheId/accessId/UUID/nomDuFichier
            String virtualPath = "/" + accountId + "/" + containerId + "/" + accessId + "/" + uuid + "/" + URLEncoder.encode(filename, "UTF-8");
            LOGGER.info("Chemin virtuel : {}", virtualPath);

            // Constitution de l'URL d'appel
            URL url = new URL(AfServletGouvPropertiesResolver.getFileUrl() + virtualPath);
            LOGGER.info("URL d'appel : {}", url);

            // Extraction du demandeId si le client le connaît déjà et l'a fourni à AFS
            String demandeId = null;
            Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                String headerName = headers.nextElement();
                if (headerName.startsWith(AppFactoryServletUtils.FILE_METADATA_DEMANDEID)) {
                    demandeId = request.getHeader(headerName);
                }
            }

            // Constitution de la requête
            HttpClient client = HttpClientBuilder.create().build();
            Part part = request.getParts().iterator().next();
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("data",
                    new InputStreamBody(part.getInputStream(), part.getContentType(), part.getSubmittedFileName()));
            HttpEntity multipart = builder.build();
            HttpPost postRequest = new HttpPost(url.toString());
            postRequest.setEntity(multipart);
            // Renseigner le demandeId le cas échéant
            if (demandeId != null) {
                postRequest.setHeader(AppFactoryServletUtils.FILE_METADATA_DEMANDEID, demandeId);
            }

            // Rajouter l'information si le fichier a été scanné par VSCAN ou pas
            postRequest.setHeader(AppFactoryServletUtils.FILE_METADATA_SCANEXECUTE, activationVscan + "");

            postRequest.setHeader(HttpHeaders.AUTHORIZATION, AppFactoryServletUtils.getAuthHeader(ServiceTarget.FILE));

            LOGGER.info("Appel du WS FILE");
            HttpResponse postResponse = client.execute(postRequest);

            // Constitution de la réponse en redirigeant la réponse du WS ansi que son code réponse
            LOGGER.info("Constitution de la réponse pour retour au client");
            response.setContentType("application/json");

            int statusCode = postResponse.getStatusLine().getStatusCode();
            response.setStatus(statusCode);

            if (statusCode == HttpServletResponse.SC_OK || statusCode == HttpServletResponse.SC_CREATED) {
                // Si tout s'est bien passé, alors on forme une réponse différente que celle qui nous est retournée par
                // FILE
                response.setContentType("application/json");
                mapper = new ObjectMapper();
                // Répondre accessId/uuid/nomDuFichier
                FileUploadResponseDTO responseObj = new FileUploadResponseDTO(accessId + "/" + uuid + "/" + filename);
                String responseStr = mapper.writeValueAsString(responseObj);
                response.getOutputStream().write(responseStr.getBytes());
            } else {
                LOGGER.error("Status code : {}", statusCode);
                // S'il y a eu un problème, alors on retourne le message d'erreur au client
                IOUtils.copy(postResponse.getEntity().getContent(), response.getOutputStream());
            }

        } catch (Exception e) {
            response = AppFactoryServletUtils.logAndSendError(LOGGER, response, HttpStatus.SC_INTERNAL_SERVER_ERROR,
                    "Erreur interne: ", e);
        }

        // Supression des sessions inutilisées chaque 10 requêtes d'upload
        if (compteurCleanSessions > 50) {
            reinitialierSessionsInutilisees();
        }

        // Ajout dans l'historique par session
        ajouterCompteurUpload(session);

        LOGGER.info("====================== Fin /fileupload doPost()");
    }

    private boolean estExtensionDansWhitelist (String filename) {
        String[] filenameSplit = filename.split("\\.");
        String fileExtension = filenameSplit[filenameSplit.length-1].toLowerCase();
        return getExtensionsWhitelist().contains(fileExtension);
    }


    private List<String> getExtensionsWhitelist() {
        List<String> extensions = new ArrayList<>();
        PropertiesDTO extensionsProperty = getPropriete(EXTENSIONS_WHITELIST);

        if(extensionsProperty != null) {
            String propertyString = extensionsProperty.getValue().replace("*.","").replace(" ","");
            String[] types = propertyString.split(",");
            Collections.addAll(extensions, types);
        }

        return extensions;
    }

    private PropertiesDTO getPropriete(String propriete) {
        List<PropertiesDTO> properties = getAfApiClient().getFrontProperties();
        List<PropertiesDTO> propFiltrees = properties.stream().filter(prop -> prop.getKey().equals(propriete)).collect(Collectors.toList());
        return (propFiltrees.size() > 0) ? propFiltrees.get(0) : null;
    }

    private synchronized static void ajouterCompteurUpload(HttpSession session) {
        FileUploadCompteurDTO compteurUpload = usagersFileUploadCompteurs.get(session);
        if (compteurUpload == null) {
            compteurUpload = new FileUploadCompteurDTO();
            compteurUpload.setCompteur(0);
            compteurUpload.setDatePremierUpload(LocalDateTime.now());
        }
        // Ajouter au compteur qu'un nouveau fichier a été uploadé
        compteurUpload.setCompteur(compteurUpload.getCompteur() + 1);
        usagersFileUploadCompteurs.put(session, compteurUpload);
        compteurCleanSessions++;
    }

    /**
     * Methode qui parcours toutes les sessions stockées et supprime les entrées qui ne servent plus. ex:
     *      Une session dont la date du premier upload > x secondes
     */
    private synchronized static void reinitialierSessionsInutilisees() {
        for(Iterator<Map.Entry<HttpSession, FileUploadCompteurDTO>> it = usagersFileUploadCompteurs.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<HttpSession, FileUploadCompteurDTO> entry = it.next();
            LocalDateTime datePremierUpload = entry.getValue().getDatePremierUpload();
            Duration duration = Duration.between(datePremierUpload, LocalDateTime.now());
            int tempsParIntervalle = Integer.parseInt(AfServletGouvPropertiesResolver.getTempsIntervalleUpload());
            if (duration.toMillis() > tempsParIntervalle) {
                it.remove();
            }
        }
        compteurCleanSessions = 0;
    }
}
