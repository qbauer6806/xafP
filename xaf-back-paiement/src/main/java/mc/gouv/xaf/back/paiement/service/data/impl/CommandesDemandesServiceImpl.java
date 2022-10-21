package mc.gouv.xaf.back.paiement.service.data.impl;

import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeDemandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandesDemandesServiceImpl implements CommandesDemandesService {

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Override
    public CommandeDemandeDTO getDerniereCommandeDemande(Integer demandeId) {
        List<CommandeDemandeBO> commandeDemandeBOS = commandeDemandeRepository.findByDemande_PkDemandesOrderByCommande_DateCreationDesc(demandeId);
        return CommandeDemandeTransformer.bo2Dto(commandeDemandeBOS.get(0));
    }

}
