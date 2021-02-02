package mc.gouv.xaf.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

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
    
    @ModelAttribute(name = "contactSupportUrl")
    public String getContactSupportUrl() {
	return gouvPropertiesResolver.getContactSupportUrl();
    }
}
