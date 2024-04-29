package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import mc.gouv.xaf.back.service.DynamicJSService;

/**
 * Contrôleur permettant de retourner le fichier dynamicjs.js généré
 * 
 * @author qdeme
 *
 */
@GouvRestController
@RequestMapping("/dynamicjs")
public class DynamicJSController {
    
    @Autowired
    private DynamicJSService dynamicJSService;

    @GetMapping(value = "/dynamicjs.js", produces = "application/javascript")
    public ResponseEntity<String> getResponse() {
        String js = dynamicJSService.getResponse();
        return new ResponseEntity<>(js, null, HttpStatus.OK);
    }
    
}
