package mc.gouv.af.back.config.advice;

import mc.gouv.Static;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AfBackControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackControllerAdvice.class);

    @Value("${application.name}")
    private String applicationName;

    private String applicationPrefix = StringUtils.EMPTY;

    @ModelAttribute(name = "demarcheId")
    public String addDemarcheId() {

        if (StringUtils.isNotBlank(applicationName)) {
            applicationPrefix = "." + applicationName;
        }
        return Static.getValue("mc.gouv" + applicationPrefix + ".backserver.demarcheId");
    }
}
