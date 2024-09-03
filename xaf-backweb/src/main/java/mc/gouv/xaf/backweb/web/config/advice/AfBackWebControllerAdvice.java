package mc.gouv.xaf.backweb.web.config.advice;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AfBackWebControllerAdvice {

    @Autowired
    BackGouvPropertiesResolver gouvPropertiesResolver;

    @ModelAttribute(name = "helpUrl")
    public String addHelpUrl() {

        return gouvPropertiesResolver.getHelpUrl();
    }

    @ModelAttribute(name = "contactSupportUrl")
    public String getContactSupportUrl() {
        return gouvPropertiesResolver.getContactSupportUrl();
    }
}
