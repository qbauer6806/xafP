package mc.gouv.xaf.front.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.front.properties.FrontGouvPropertiesResolver;
import mc.gouv.xaf.front.util.XafFrontserverUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/properties")
@RequiredArgsConstructor
public class PropertiesController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesController.class);

    private final FrontGouvPropertiesResolver propertiesResolver;
    private final XafFrontserverUtils xafFrontserverUtils;

    @GetMapping
    public ResponseEntity<String> doGet() {
        LOGGER.info("====================== /properties doGet()");
        LOGGER.info("Appel de la démarche afin de récupérer les propriétés FRONT ...");
        try {
            List<PropertiesDTO> properties = xafFrontserverUtils.getAfApiClient().getFrontProperties();
            LOGGER.info("Ajout des properties du fichier frontserver.properties...");
            properties.addAll(propertiesResolver.getFrontProperties());
            ObjectMapper mapper = new ObjectMapper();
            String repJson = mapper.writeValueAsString(properties);
            LOGGER.info("====================== Fin /properties doGet()");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(repJson);
        } catch (Exception e) {
            LOGGER.error("PropertiesServlet - Une erreur est survenue lors de l'appel à la méthode GET", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}



