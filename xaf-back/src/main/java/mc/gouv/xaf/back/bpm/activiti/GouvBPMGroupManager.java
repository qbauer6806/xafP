package mc.gouv.xaf.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.impl.GroupQueryImpl;
import org.activiti.engine.impl.QueryVariableValue;
import org.activiti.engine.impl.TaskQueryImpl;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.GroupEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.logon.shared.Droit;
import mc.gouv.logon.shared.Role;
import mc.gouv.logon.shared.User;
import mc.gouv.servicerest.usager.model.UsagerBean;

/**
 * 
 * Provider de groupes custom du gouvernement pour Activiti
 * 
 * @author qdeme
 *
 */
@Component
public class GouvBPMGroupManager extends GroupEntityManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMGroupManager.class);

    @Autowired
    private UsagersCache usagerCache;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Override
    public long findGroupCountByQueryCriteria(GroupQueryImpl query) {
        return findGroupByQueryCriteria(query, null).size();
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        LOGGER.debug("GouvBPMGroupManager.findGroupsByUser(" + userId + ")");

        // Il faut récupérer le code appli pour appeler getUserByMatricule() du Logon
        String codeAppli = null;
        List<QueryVariableValue> variables = ((TaskQueryImpl) Context.getCommandContext().getCommand())
                .getQueryVariableValues();
        for (QueryVariableValue var : variables) {
            if (GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name().equals(var.getName())) {
                codeAppli = var.getTextValue();
            }
        }

        ArrayList<Group> liste = new ArrayList<Group>();
        if (codeAppli != null) {

            User user = utilisateursCache.get(userId);
            if (user != null) {
                Set<Role> roles = user.getRoles();
                if (roles != null) {
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
            } else {
                // On teste si c'est un usager
                UsagerBean usager = usagerCache.get(Integer.parseInt(userId));

                if (usager != null) {
                    // Ajout au groupe usager
                    GroupEntity ge = new GroupEntity();
                    ge.setId("USAGER");
                    ge.setName("USAGER");
                    liste.add(ge);
                }
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
