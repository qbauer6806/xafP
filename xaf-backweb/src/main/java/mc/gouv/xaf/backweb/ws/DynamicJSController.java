package mc.gouv.xaf.backweb.ws;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Contrôleur permettant de retourner le fichier dynamicjs.js généré
 *
 * @author qdeme
 */
@GouvRestController
@RequestMapping("/dynamicjs")
@RequiredArgsConstructor
public class DynamicJSController {

    private final DynamicJSService dynamicJSService;

    @GetMapping(value = "/dynamicjs.js", produces = "application/javascript")
    public ResponseEntity<String> getResponse() {
        String js = dynamicJSService.getResponse();
        return ResponseEntity.ok(js);
    }

}
