package mc.gouv.xaf.backweb.ws;

import mc.gouv.xaf.backweb.web.config.annotation.GouvRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.es.RechercheDynamicJSService;

@GouvRestController
@RequestMapping("/recherchedynamicjs")
@Conditional(IndexationEnabledCondition.class)
public class RechercheDynamicJSController {

    @Autowired
    private RechercheDynamicJSService rechercheDynamicJSService;

    @GetMapping(value = "/dynamicjs.js", produces = "application/javascript")
    public ResponseEntity<String> getResponse() {
        String js = rechercheDynamicJSService.getResponse();
        return new ResponseEntity<>(js, null, HttpStatus.OK);
    }

}
