package mc.gouv.af.back.service.transformer;

import mc.gouv.af.back.data.es.model.AgentEsDTO;
import mc.gouv.logon.shared.User;

public class AgentEsTransformer {

    private AgentEsTransformer() {
    }

    public static AgentEsDTO bo2Dto(User bo) {

        if (bo == null) {
            return null;
        }

        AgentEsDTO agent = new AgentEsDTO();
        agent.setFonction(bo.getFonction());
        agent.setMail(bo.getMail());
        agent.setMatricule(bo.getMatricule());
        agent.setNom(bo.getNom());
        agent.setNomNaissance(bo.getNomNaissance());
        agent.setNomUsage(bo.getNomUsage());
        agent.setPrenom(bo.getPrenom());

        return agent;

    }
}
