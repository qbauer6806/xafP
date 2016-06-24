package mc.gouv.af.back.bpm.activiti;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.GroupIdentityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Factory pour le GouvGroupManager
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMGroupManagerFactory implements SessionFactory {

    @Autowired
    private GouvBPMGroupManager groupManager;
    
    @Override
    public Class<?> getSessionType() {
        return GroupIdentityManager.class;
    }

    @Override
    public Session openSession() {
        return groupManager;
    }

}
