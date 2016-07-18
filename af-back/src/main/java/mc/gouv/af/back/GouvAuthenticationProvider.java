package mc.gouv.af.back;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import mc.gouv.af.back.util.LogonProxy;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.model.Droit;
import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;

/**
 * Composant Spring permettant de lier l'application à Logon pour la sécurité
 * 
 * @author qdeme
 *
 */
@Component
public class GouvAuthenticationProvider implements AuthenticationProvider {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvAuthenticationProvider.class);
    
    private final static String GOUV_SESSION_HEADER = "ksession";
    
    @Autowired
    private LogonProxy logonProxy;
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        
        LOGGER.info("GouvAuthenticationProvider.authenticate(" + authentication + ")");
        
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        String gouvSession = request.getHeader(GOUV_SESSION_HEADER);
//        
//        if (StringUtils.isBlank(gouvSession)) {
//            LOGGER.error("Session Logon inexistante dans la requête");
//            return null;
//        }
//        LOGGER.info("Gouv session = " + gouvSession);
//        
//        User user = null;
//        try {
//            user = logonProxy.getLoggedUser(gouvSession);
//        } catch (RestException e) {
//            LOGGER.error("Une erreur s'est produite lors de l'appel à Logon", e);
//            return null;
//        }
//        
//        if (user == null) {
//            LOGGER.info("Aucun utilisateur n'a pu être récupéré à partir de la session");
//            return null;
//        }
//        LOGGER.info("Utilisateur retrouvé suite à l'appel à Logon : " + user);
        
        // Constitution de l'Authentication Spring à l'aide des données obtenues de Logon
        
//        String name = user.getMatricule();
        String name = "user";
        String password = "none";
        
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
//        Set<Role> roles = user.getRoles();
//        for (Role role : roles) {
//            // Il faut que ces droits concernent l'application en question
//            // TODO Comment récupérer le code Appli ici ?
//            if (role.getAppli().getCode().equals("CITES")) {
//                Set<Droit> droits = role.getDroits();
//                for (Droit droit : droits) {
//                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + droit.getCode()));
//                }
//            }
//        }

        return new UsernamePasswordAuthenticationToken(name, password, grantedAuthorities);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
