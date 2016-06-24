package mc.gouv.af.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Provider de groupes custom du gouvernement pour Activiti
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMGroupManager extends GroupEntityManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMGroupManager.class);

    @Override
    public Group createNewGroup(String groupId) {
        // TODO Auto-generated method stub
        return super.createNewGroup(groupId);
    }

    @Override
    public void insertGroup(Group group) {
        // TODO Auto-generated method stub
        super.insertGroup(group);
    }

    @Override
    public void updateGroup(Group updatedGroup) {
        // TODO Auto-generated method stub
        super.updateGroup(updatedGroup);
    }

    @Override
    public void deleteGroup(String groupId) {
        // TODO Auto-generated method stub
        super.deleteGroup(groupId);
    }

    @Override
    public GroupQuery createNewGroupQuery() {
        // TODO Auto-generated method stub
        return super.createNewGroupQuery();
    }

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        LOGGER.info("findGroupByQueryCriteria()");
        if (query.getId().equals("testGroup")) {
            GroupEntity ge = new GroupEntity();
            ge.setId(query.getId());
            ge.setName(query.getName());
            ge.setType(query.getType());
            ArrayList<Group> liste = new ArrayList<Group>();
            liste.add(ge);
            return liste;
        }
        return super.findGroupByQueryCriteria(query, page);
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        // TODO Auto-generated method stub
        LOGGER.info("findGroupCountByQueryCriteria()");
        return super.findGroupCountByQueryCriteria(query);
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        // TODO Auto-generated method stub
        LOGGER.info("findGroupsByUser(" + userId + ")");
        if (userId.equals("11950")) {
            GroupEntity ge = new GroupEntity();
            ge.setId("testGroup");
            ge.setName("testGroup");
            ge.setType("type");
            ArrayList<Group> liste = new ArrayList<Group>();
            liste.add(ge);
            return liste;
        }
        return super.findGroupsByUser(userId);
    }

}
