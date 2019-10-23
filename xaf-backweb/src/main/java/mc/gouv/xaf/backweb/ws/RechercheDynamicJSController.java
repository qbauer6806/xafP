package mc.gouv.xaf.backweb.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.es.RechercheDynamicJSService;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

@GouvRestController
@RequestMapping("/recherchedynamicjs")
@Conditional(IndexationEnabledCondition.class)
public class RechercheDynamicJSController {

    @Autowired
    private RechercheDynamicJSService rechercheDynamicJSService;

    @RequestMapping(value = "/dynamicjs.js", method = RequestMethod.GET, produces = "application/javascript")
    public ResponseEntity<String> getResponse() {

        String js = rechercheDynamicJSService.getResponse();

        return new ResponseEntity<>(js, null, HttpStatus.OK);
    }

}
