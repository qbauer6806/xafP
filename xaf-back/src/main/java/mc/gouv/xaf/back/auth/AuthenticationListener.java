package mc.gouv.xaf.back.auth;

import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private final UtilisateursCache utilisateursCache;

    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        // Dans le cas ou l'utilisateur s'est loggué via logon et non un appel WS avec HabApiController/StageApiController/*ApiController
        if (principal instanceof User) {
            User u = (User) event.getAuthentication().getPrincipal();
            utilisateursCache.add(u.getMatricule(), u);
        }

    }

}
