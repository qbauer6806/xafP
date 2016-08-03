package mc.gouv.af.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.QueryVariableValue;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.af.back.util.LogonProxy;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.model.Droit;
import mc.gouv.logon.model.Role;
import mc.gouv.logon.model.User;

/**
 * Provider de groupes custom du gouvernement pour Activiti
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMGroupManager extends GroupEntityManager {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMGroupManager.class);
    
    @Autowired
    private LogonProxy logonProxy;

    @Override
    public List<Group> findGroupByQueryCriteria(GroupQueryImpl query, Page page) {
        LOGGER.info("GouvBPMGroupManager.findGroupByQueryCriteria()");
        
        // Il faut récupérer le code appli pour appeler getUserByMatricule() du Logon
        // HACK On utilise un critère "groupType" pour faire passer l'info du code appli...
        String codeAppli = query.getType();
        
        ArrayList<Group> liste = new ArrayList<Group>();
        try {
            List<Droit> droits = logonProxy.getDroitsByCodeAppli(codeAppli);
            for (Droit d : droits) {
                GroupEntity ge = new GroupEntity();
                ge.setId(d.getId().toString());
                ge.setName(d.getCode());
                ge.setType(query.getType());
                // Si on a en critère un code Droit, il ne faut retourner que celui-là (singleResult())
                if (!StringUtils.isBlank(query.getId()) && query.getId().equals(d.getCode())) {
                    liste.add(ge);
                }
            }
        } catch (RestException e) {
            e.printStackTrace();
        }
        return liste;
    }

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        return findGroupByQueryCriteria(query, null).size();
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        LOGGER.info("GouvBPMGroupManager.findGroupsByUser(" + userId + ")");
        
        // Il faut récupérer le code appli pour appeler getUserByMatricule() du Logon
        String codeAppli = null;
        List<QueryVariableValue> variables = ((TaskQueryImpl)Context.getCommandContext().getCommand()).getQueryVariableValues();
        for (QueryVariableValue var : variables) {
            if (GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name().equals(var.getName())) {
                codeAppli = var.getTextValue();
            }
        }
        
        ArrayList<Group> liste = new ArrayList<Group>();
        if (codeAppli != null) {
            try {
                User user = logonProxy.getUserByMatricule(userId);
                if (user != null) {
                    Set<Role> roles = user.getRoles();
                    for (Role role : roles) {
                        // Il faut que ces droits concernent l'application en question
                        if (role.getAppli().getCode().equals(codeAppli)) {
                            Set<Droit> droits = role.getDroits();
                            for (Droit droit : droits) {
                                GroupEntity ge = new GroupEntity();
                                ge.setId(droit.getCode());
                                ge.setName(droit.getTitre());
                                liste.add(ge);
                            }
                        }
                    }
                }
            } catch (RestException e) {
                // TODO
                e.printStackTrace();
            }
        }
        return liste;
    }
    
    @Override
    public Group createNewGroup(String groupId) {
        return super.createNewGroup(groupId);
    }

    @Override
    public void insertGroup(Group group) {
        super.insertGroup(group);
    }

    @Override
    public void updateGroup(Group updatedGroup) {
        super.updateGroup(updatedGroup);
    }

    @Override
    public void deleteGroup(String groupId) {
        super.deleteGroup(groupId);
    }

    @Override
    public GroupQuery createNewGroupQuery() {
        return super.createNewGroupQuery();
    }

}
