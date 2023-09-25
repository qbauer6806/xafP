package mc.gouv.xaf.back.bpm.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.activiti.engine.identity.Group;
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

import mc.gouv.logon.shared.Droit;
import mc.gouv.logon.shared.Role;
import mc.gouv.logon.shared.User;
import mc.gouv.xaf.back.bpm.GouvBPMProcessVariableTypeEnum;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

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
        LOGGER.debug("GouvBPMGroupManager.findGroupsByUser({})", userId);

        // Il faut récupérer le code appli pour appeler getUserByMatricule() du Logon
        String codeAppli = null;
        List<QueryVariableValue> variables = ((TaskQueryImpl) Context.getCommandContext().getCommand())
                .getQueryVariableValues();
        for (QueryVariableValue v : variables) {
            if (GouvBPMProcessVariableTypeEnum.MC_CODEAPPLI.name().equals(v.getName())) {
                codeAppli = v.getTextValue();
            }
        }

        List<Group> liste = new ArrayList<>();
        if (codeAppli != null) {

            User user = utilisateursCache.get(userId);
            if (user != null) {
                addAllRoles(user, liste, codeAppli);
            } else {
                // On teste si c'est un usager
            	GichuniUsagerDTO usager = usagerCache.get(Integer.parseInt(userId));

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

    private void addAllRoles(User user, List<Group> liste, String codeAppli) {
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
    }

}
