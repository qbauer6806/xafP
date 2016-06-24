package mc.gouv.af.back;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.util.LogonProxy;
import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;
import mc.gouv.logon.rest.client.RestException;

/**
 * Mock de LogonProxy afin de mocker l'appel à Logon pour les tests
 * 
 * @author qdeme
 *
 */
@Component
public class LogonProxyImplTest implements LogonProxy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogonProxyImplTest.class);

    /**
     * Production d'un user Logon de test pour un certain matricule
     */
    @Override
    public User getUserByMatricule(String matricule) throws RestException {
        LOGGER.info("LogonProxyImplTest.getUserByMatricule(" + matricule + ")");
        if (AfBPMTest.USER_MATRICULE.equals(matricule)) {
            User user = new User();
            user.setMatricule(matricule);
            user.setMail("test@gouv.mc");
            user.setId(1);
            return user;
        }
        return null;
    }

    @Override
    public User getUserByOpid(String opid) throws RestException {
        return null;
    }

    @Override
    public List<User> getUsersWithDroit(String codeAppli, String codeDroit) throws RestException {
        return null;
    }

    @Override
    public List<User> getUsersWithRole(Integer idRole) throws RestException {
        return null;
    }

    @Override
    public List<Role> getRolesByCodeAppli(String codeAppli) throws RestException {
        return null;
    }

    @Override
    public User getLoggedUser(String sessionId) throws RestException {
        return null;
    }

}
