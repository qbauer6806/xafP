package mc.gouv.xaf.front.controller;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
                                HttpServletRequest request) throws IOException, ServletException {
        LOGGER.info("====================== /filepreview doGet()");

        try {
            return super.doGet(accessId, uuid, filename, request, true);
        } catch (Exception e) {
            LOGGER.error("FilePreviewServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
