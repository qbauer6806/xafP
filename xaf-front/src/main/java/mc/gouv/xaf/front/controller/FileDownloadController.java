package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Servlet servant à télécharger un fichier de FILE.
 *
 * @author qdeme
 */
@Controller
public class FileDownloadController extends FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileDownloadController.class);

    @GetMapping(value = { "/filedownload",
            "/filedownload/{accessId}/{uuid}/{filename}" }, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity doGet(@PathVariable(required = false) String accessId,
            @PathVariable(required = false) String uuid, @PathVariable(required = false) String filename,
            HttpServletRequest request) throws IOException {
        LOGGER.info("====================== /filedownload doGet()");

        try {
            return super.doGet(accessId, uuid, filename, request, false);
        } catch (Exception e) {
            LOGGER.error("FileDownloadServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
