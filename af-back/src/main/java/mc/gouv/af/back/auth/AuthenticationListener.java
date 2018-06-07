package mc.gouv.af.back.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.logon.shared.User;

@Component
public class AuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {

    @Autowired
    private UtilisateursCache utilisateursCache;

    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        // Dans le cas ou l'utilisateur s'est loggué via logon et non un appel WS avec HabApiController/StageApiController/*ApiController
        if (principal instanceof User) {
            User u = (User) event.getAuthentication().getPrincipal();
            utilisateursCache.add(u.getMatricule(), u);
        }

    }

}
