package mc.gouv.af.back.config.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;

@ControllerAdvice
public class AfBackControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackControllerAdvice.class);

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
