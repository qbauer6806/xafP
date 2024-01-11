package mc.gouv.xaf.backweb.web.config.piwik;

import mc.gouv.logon.shared.User;
import mc.gouv.tools.piwik.PiwikTracker;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class PiwikTrackerControllerAdvice {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @ModelAttribute(name = "piwikTracker")
    public PiwikTracker addPiwikTracker() {
        String piwikUserId;
        if (SecurityContextHolder.getContext() != null && SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                var user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                piwikUserId = user.getMatricule();
                return new PiwikTracker(gouvPropertiesResolver.getPiwikUrl(), gouvPropertiesResolver.getPiwikSiteId(), piwikUserId);
            }
        }
        return null;
    }
}
