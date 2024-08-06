package mc.gouv.xaf.front.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 
 * Servlet servant à visualiser dans le navigateur un fichier de FILE.
 * 
 * @author uek
 *
 */
@Controller
public class FilePreviewController extends FileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FilePreviewController.class);

    @GetMapping(value = {"/filepreview", "/filepreview/{accessId}/{uuid}/{filename}"})
    public ResponseEntity doGet(@PathVariable(required = false) String accessId,
                                @PathVariable(required = false) String uuid,
                                @PathVariable(required = false) String filename,
                                HttpServletRequest request) throws IOException {
        LOGGER.info("====================== /filepreview doGet()");

        try {
            return super.doGet(accessId, uuid, filename, request, true);
        } catch (Exception e) {
            LOGGER.error("FilePreviewServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
