package mc.gouv.af.back.config.advice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import mc.gouv.af.back.properties.GouvPropertiesResolver;

@ControllerAdvice
public class AfBackControllerAdvice {

    @Autowired
    GouvPropertiesResolver gouvPropertiesResolver;

    @ModelAttribute(name = "demarcheId")
    public String addDemarcheId() {

        return gouvPropertiesResolver.getDemarcheId();
    }

    @ModelAttribute(name = "helpUrl")
    public String addHelpUrl() {

        return gouvPropertiesResolver.getHelpUrl();
    }
}
