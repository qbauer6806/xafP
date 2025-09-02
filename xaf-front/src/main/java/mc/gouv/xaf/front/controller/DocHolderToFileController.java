package mc.gouv.xaf.front.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import mc.gouv.xaf.front.dto.UsagerInfosDTO;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.FileControllerUtils;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.util.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/tofile")
public class DocHolderToFileController extends AbstractXafController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderToFileController.class);

    @Autowired
    private XafFrontserverUtils xafFrontserverUtils;

    @Autowired
    private FrontGouvPropertiesResolver frontGouvPropertiesResolver;

    @Autowired
    private FileControllerUtils fileControllerUtils;

    /**
     * Méthode qui permet de transférer un fichier du porte-document à FILE
     */
    @PostMapping
    protected ResponseEntity doPost(HttpServletRequest req) throws IOException {
        LOGGER.info("====================== {} doPost()", req.getServletPath());

        UsagerInfosDTO usagerInfosDTO = xafFrontserverUtils.getLoggedUser(req);
        if (usagerInfosDTO == null) {
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.UNAUTHORIZED,
                    SharedMessages.UTILISATEUR_NON_AUTORISE);
        }

        Gson gson = new Gson();
        JsonObject jsonObject;

        LOGGER.info("Déserialization de la réponse");
        try (Reader reader = new InputStreamReader(req.getInputStream())) {
            jsonObject = gson.fromJson(reader, JsonObject.class);
        } catch (JsonParseException jpe) {
            LOGGER.error("Erreur lors de la déserialisation.", jpe);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        JsonElement urlElement = jsonObject.get("url");
        String fileUrl = urlElement != null ? urlElement.getAsString() : null;

        if (StringUtils.isEmpty(fileUrl)) {
            LOGGER.error("Erreur lors de la récupération du paramètre 'url' => paramètre vide ou inconnu.");
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        String docHolderFileServiceUrl = frontGouvPropertiesResolver.getPorteDocUrl() + "/file";
        String filename = fileControllerUtils.getFilename(fileUrl);

        try {
            LOGGER.info("Téléchargement du fichier {} depuis le porte-documents", fileUrl);
            ClassicHttpResponse docholderResponse = (ClassicHttpResponse) fileControllerUtils.downloadFromDocHolder(
                    docHolderFileServiceUrl, fileUrl, usagerInfosDTO.getTokenInfo().getAccessToken());

            if (docholderResponse.getCode() == 200) {
                LOGGER.info("Téléversement du fichier {} dans FILE", filename);
                // encodage du nom de fichier
                String safeFileName = FileNameUtils.getSafeFileName(filename);
                return fileControllerUtils.uploadToFILE(usagerInfosDTO, safeFileName, "AUTRES",
                        docholderResponse.getEntity().getContent());

            } else {
                LOGGER.info("====================== Fin {} doPost()", req.getServletPath());
                return ResponseEntity.status(docholderResponse.getCode())
                        .body(new String(docholderResponse.getEntity().getContent().readAllBytes(),
                                StandardCharsets.UTF_8));
            }
        } catch (IOException | URISyntaxException | UnsupportedOperationException e) {
            LOGGER.error("Une erreur est survenue lors du téléchargement du fichier", e);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.INTERNAL_SERVER_ERROR,
                    SharedMessages.ERREUR_INTERNE);
        }
    }
}
