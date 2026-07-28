package mc.gouv.xaf.front.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/doc-holder/tofile")
@RequiredArgsConstructor
public class DocHolderToFileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocHolderToFileController.class);

    private final XafFrontserverUtils xafFrontserverUtils;
    private final FrontGouvPropertiesResolver frontGouvPropertiesResolver;
    private final FileControllerUtils fileControllerUtils;

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

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;

        LOGGER.info("Déserialization de la réponse");
        try (Reader reader = new InputStreamReader(req.getInputStream())) {
            jsonNode = objectMapper.readTree(reader);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la déserialisation.", e);
            return xafFrontserverUtils.logAndSendError(LOGGER, HttpStatus.BAD_REQUEST,
                    SharedMessages.REQUETE_MALFORMEE);
        }

        JsonNode urlNode = jsonNode.get("url");
        String fileUrl = (urlNode != null && !urlNode.isNull()) ? urlNode.asString() : null;

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
