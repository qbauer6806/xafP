package mc.gouv.af.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.activiti.engine.impl.persistence.entity.UserEntityManager;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMException;
import mc.gouv.af.back.util.LogonProxy;
import mc.gouv.logon.apiclient.RestException;

/**
 * Provider d'utilisateurs custom du gouvernement pour Activiti
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMUserManager extends UserEntityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMUserManager.class);
    
    @Autowired
    private LogonProxy logonProxy;

    @Override
    public UserEntity findUserById(String userLogin) {

        LOGGER.info("GouvBPMUserManager.findUserById(" + userLogin + ")");

        mc.gouv.logon.model.User user = null;
        try {
            user = logonProxy.getUserByMatricule(userLogin);
        } catch (RestException e) {
            e.printStackTrace();
        }

        if (user != null && user.getId() != null) {
            UserEntity ue = new UserEntity();
            ue.setId(userLogin);
            // Logon ne possède pas d'informations Nom et Prénom séparées
            // ue.setFirstName("ker");
            // ue.setLastName("mit");
            ue.setEmail(user.getMail());
            ue.setRevision(0);
            return ue;
        }

        return null;
    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page) {
        
        LOGGER.info("GouvBPMUserManager.findUserByQueryCriteria(query)");
        
        List<User> userList = new ArrayList<User>();
        UserQueryImpl userQuery = (UserQueryImpl) query;
        if (StringUtils.isNotEmpty(userQuery.getId())) {
            userList.add(findUserById(userQuery.getId()));
            return userList;
        } else if (StringUtils.isNotEmpty(userQuery.getLastName())) {
            userList.add(findUserById(userQuery.getLastName()));
            return userList;
        } else {
            return null;
        }
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query) {
        return findUserByQueryCriteria(query, null).size();
    }

    @Override
    public Boolean checkPassword(String userId, String password) {
        throw new GouvBPMException("checkPassword() not supported");
    }

    @Override
    public Picture getUserPicture(String userId) {
        throw new GouvBPMException("getUserPicture() not supported");
    }
    
    @Override
    public User createNewUser(String userId) {
        throw new GouvBPMException("createNewUser() not supported");
    }

    @Override
    public void insertUser(User user) {
        throw new GouvBPMException("insertUser() not supported");
    }

    @Override
    public void updateUser(User updatedUser) {
        throw new GouvBPMException("updateUser() not supported");
    }

    @Override
    public void deleteUser(String userId) {
        throw new GouvBPMException("deleteUser() not supported");
    }

}
