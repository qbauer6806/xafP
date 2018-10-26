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
        agent.setCivilite((bo.getCivilite() != null) ? bo.getCivilite().getLibelle() : null);
        agent.setDateCreation(bo.getDateCrea());
        agent.setFonction(bo.getFonction());
        agent.setMail(bo.getMail());
        agent.setMatricule(bo.getMatricule());
        agent.setNom(bo.getNom());
        agent.setNomNaissance(bo.getNomNaissance());
        agent.setNomUsage(bo.getNomUsage());
        agent.setPrenom(bo.getPrenom());
        agent.setService(bo.getService());
        agent.setTelMobilePro(bo.getTelMobilePro());
        agent.setTelPro(bo.getTelPro());
        agent.setUfCode(bo.getUfCode());

        return agent;

    }
}
