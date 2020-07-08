package mc.gouv.xaf.backweb.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

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

    @RequestMapping(value = "/dynamicjs.js", method = RequestMethod.GET, produces = "application/javascript")
    public ResponseEntity<String> getResponse() {
        
        String js = dynamicJSService.getResponse();
        
        return new ResponseEntity<String>(js, null, HttpStatus.OK);
    }
    
}
