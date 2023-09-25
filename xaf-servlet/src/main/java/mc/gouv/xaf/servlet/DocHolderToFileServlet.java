package mc.gouv.xaf.servlet;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mc.gouv.xaf.servlet.dto.UsagerInfosDTO;
import mc.gouv.xaf.servlet.properties.AfServletGouvPropertiesResolver;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.servlet.util.FileServletUtils;
import mc.gouv.xaf.shared.SharedMessages;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;

public class DocHolderToFileServlet extends AbstractAfServlet {
    private static final long serialVersionUID = -314577095316396789L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderFileServlet.class);

    /**
     * Méthode qui permet de transférer un fichier du porte-document à FILE
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = AppFactoryServletUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_UNAUTHORIZED, SharedMessages.UTILISATEUR_NON_AUTORISE);
            return;
        }

        Gson gson = new Gson();
        JsonObject jsonObject;

        LOGGER.info("Déserialization de la réponse");
        try (Reader reader = new InputStreamReader(req.getInputStream())) {
            jsonObject = gson.fromJson(reader, JsonObject.class);
        } catch (JsonParseException jpe) {
            LOGGER.error("Erreur lors de la déserialisation.", jpe);
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        JsonElement urlElement = jsonObject.get("url");
        String fileUrl = urlElement != null ? urlElement.getAsString() : null;

        if (StringUtils.isEmpty(fileUrl)) {
            LOGGER.error("Erreur lors de la récupération du paramètre 'url' => paramètre vide ou inconnu.");
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.REQUETE_MALFORMEE);
            return;
        }

        String docHolderFileServiceUrl = AfServletGouvPropertiesResolver.getPorteDocUrl() + "/file";
        String filename = FileServletUtils.getFilename(fileUrl);
        // Récupération du nom du fichier à envoyer


        /*LOGGER.info("Vérification de la taille du fichier...");
        if (!FileServletUtils.tailleFichierValide(filePart)) {
            LOGGER.info("La taille du fichier depasse la taille max definie dans les propriétés (taille du fichier : {} B)", filePart.getSize());
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_FORBIDDEN, SharedMessages.FICHIER_TROP_GRAND);
            return;
        }

        LOGGER.info("Vérification de la taille du fichier...");
        if (!FileServletUtils.tailleFichierValide(filePart)) {
            LOGGER.info("La taille du fichier depasse la taille max definie dans les propriétés (taille du fichier : {} B)", filePart.getSize());
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_FORBIDDEN, SharedMessages.FICHIER_TROP_GRAND);
            return;
        }

        String safeFilename = filename.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
        LOGGER.info("Vérification du type pour le fichier {} ...", safeFilename);
        if (!FileServletUtils.estExtensionDansWhitelist(filename)) {
            LOGGER.info("Le type de fichier ne correspond pas aux types whitelistés ({})", FileServletUtils.getExtensionsWhitelist());
            AppFactoryServletUtils.logAndSendError(LOGGER, resp, HttpStatus.SC_BAD_REQUEST, SharedMessages.FICHIER_TYPE_EXTENTION_INVALIDE);
            return;
        }*/

        try {

            HttpResponse response = FileServletUtils.downloadFromDocHolder(docHolderFileServiceUrl, fileUrl, "Bearer " + usagerInfosDTO.getTokenInfo().getAccessToken());

            if (response.getStatusLine().getStatusCode() == 200) {
                FileServletUtils.uploadToFILE(resp, getServletContext(), usagerInfosDTO, filename, "AUTRES", response.getEntity().getContent());
            } else {
                resp.setStatus(400);
            }

        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        resp.setStatus(200);

        LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
    }
}
