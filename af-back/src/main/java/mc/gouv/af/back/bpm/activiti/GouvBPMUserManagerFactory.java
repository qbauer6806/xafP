package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Factory pour le GouvBPMUserManager
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMUserManagerFactory implements SessionFactory {
    
    @Autowired
    private GouvBPMUserManager userManager;

    @Override
    public Class<?> getSessionType() {
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return userManager;
    }

}
