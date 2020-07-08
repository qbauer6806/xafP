package mc.gouv.xaf.back.service.es.transformer;

import mc.gouv.xaf.back.data.es.model.AgentEsDTO;
import mc.gouv.logon.shared.User;

public class AgentEsTransformer {

    private AgentEsTransformer() {
    }

    public static AgentEsDTO bo2Dto(User bo) {

        if (bo == null) {
            return null;
        }

        AgentEsDTO agent = new AgentEsDTO();
        agent.setMail(bo.getMail());
        agent.setMatricule(bo.getMatricule());
        agent.setNom(bo.getNom());
        agent.setNomNaissance(bo.getNomNaissance());
        agent.setNomUsage(bo.getNomUsage());
        agent.setPrenom(bo.getPrenom());

        return agent;

    }
}
