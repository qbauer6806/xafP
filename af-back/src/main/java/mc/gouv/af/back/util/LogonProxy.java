package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;
import mc.gouv.logon.rest.client.RestException;

/**
 * Interface servant de proxy vers Logon afin de pouvoir mocker l'appel à logon
 * dans les tests unitaires via l'injection de dépendances Spring
 * 
 * @author qdeme
 *
 */
public interface LogonProxy {

    public User getUserByMatricule(String matricule) throws RestException;
    
    public User getUserByOpid(String opid) throws RestException;
    
    public List<User> getUsersWithDroit(String codeAppli, String codeDroit) throws RestException;
    
    public List<User> getUsersWithRole(Integer idRole) throws RestException;
    
    public List<Role> getRolesByCodeAppli(String codeAppli) throws RestException;
    
    public User getLoggedUser(String sessionId) throws RestException;
    
}
