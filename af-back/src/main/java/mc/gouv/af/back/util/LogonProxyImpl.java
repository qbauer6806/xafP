package mc.gouv.af.back.util;

import java.util.List;

import org.springframework.stereotype.Component;

import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;
import mc.gouv.logon.rest.client.RestException;
import mc.gouv.logon.rest.client.UserRest;

/**
 * Sert de proxy vers Logon afin de pouvoir mocker l'appel à logon
 * dans les tests unitaires via l'injection de dépendances Spring
 * 
 * @author qdeme
 *
 */
@Component
public class LogonProxyImpl implements LogonProxy {

    @Override
    public User getUserByMatricule(String matricule) throws RestException {
        return UserRest.getUserByMatricule(matricule);
    }

    @Override
    public User getUserByOpid(String opid) throws RestException {
        return UserRest.getUserByOpid(opid);
    }

    @Override
    public List<User> getUsersWithDroit(String codeAppli, String codeDroit) throws RestException {
        return UserRest.getUsersWithDroit(codeAppli, codeDroit);
    }

    @Override
    public List<User> getUsersWithRole(Integer idRole) throws RestException {
        return UserRest.getUsersWithRole(idRole);
    }

    @Override
    public List<Role> getRolesByCodeAppli(String codeAppli) throws RestException {
        return UserRest.getRolesByCodeAppli(codeAppli);
    }

    @Override
    public User getLoggedUser(String sessionId) throws RestException {
        return UserRest.getLoggedUser(sessionId);
    }

}
