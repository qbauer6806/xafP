package mc.gouv.af.back.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.apiclient.UserRest;
import mc.gouv.logon.model.Droit;
import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;

/**
 * Sert de proxy vers Logon afin de pouvoir mocker l'appel à logon
 * dans les tests unitaires via l'injection de dépendances Spring
 * 
 * @author qdeme
 *
 */
@Component
public class LogonProxyImpl implements LogonProxy {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(LogonProxyImpl.class);

    @Override
    public User getUserByMatricule(String matricule) throws RestException {
        LOGGER.debug("REST call to Logon : getUserByMatricule(" + matricule + ")");
        return UserRest.getUserByMatricule(matricule);
    }

    @Override
    public User getUserByOpid(String opid) throws RestException {
        LOGGER.debug("REST call to Logon : getUserByOpid(" + opid + ")");
        return UserRest.getUserByOpid(opid);
    }

    @Override
    public List<User> getUsersWithDroit(String codeAppli, String codeDroit) throws RestException {
        LOGGER.debug("REST call to Logon : getUsersWithDroit(" + codeAppli + "," + codeDroit + ")");
        return UserRest.getUsersWithDroit(codeAppli, codeDroit);
    }

    @Override
    public List<User> getUsersWithRole(Integer idRole) throws RestException {
        LOGGER.debug("REST call to Logon : getUsersWithRole(" + idRole + ")");
        return UserRest.getUsersWithRole(idRole);
    }

    @Override
    public List<Role> getRolesByCodeAppli(String codeAppli) throws RestException {
        LOGGER.debug("REST call to Logon : getRolesByCodeAppli(" + codeAppli + ")");
        return UserRest.getRolesByCodeAppli(codeAppli);
    }

    @Override
    public User getLoggedUser(String sessionId) throws RestException {
        LOGGER.debug("REST call to Logon : getLoggedUser(" + sessionId + ")");
        return UserRest.getLoggedUser(sessionId);
    }

    @Override
    public List<Droit> getDroitsByCodeAppli(String codeAppli) throws RestException {
        LOGGER.debug("REST call to Logon : getDroitsByCodeAppli(" + codeAppli + ")");
        return UserRest.getDroitsByCodeAppli(codeAppli);
    }

}
