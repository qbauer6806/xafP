package mc.gouv.xaf.back.paiement.service.data.impl;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeDemandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommandesDemandesServiceImpl implements CommandesDemandesService {

    @Autowired
    private CommandeDemandeRepository commandeDemandeRepository;

    @Autowired
    private DemandesTransformer demandesTransformer;

    @Override
    public CommandeDemandeDTO getDerniereCommandeDemande(Integer demandeId) {
        List<CommandeDemandeBO> commandeDemandeBOS = commandeDemandeRepository.findByDemande_PkDemandesOrderByCommande_DateCreationDesc(
                demandeId);
        if (!commandeDemandeBOS.isEmpty()) {
            return CommandeDemandeTransformer.bo2Dto(commandeDemandeBOS.getFirst());
        }
        return null;
    }

    @Override
    public List<DemandeBO> getDemandesFromCommande(Integer pkCommandes) {
        List<DemandeBO> demandes = new ArrayList<>();
        List<CommandeDemandeBO> commandeDemandeBOList = commandeDemandeRepository.findByCommande_PkCommandes(
                pkCommandes);
        for (CommandeDemandeBO commandeDemandeBO : commandeDemandeBOList) {
            demandes.add(commandeDemandeBO.getDemande());
        }
        return demandes;
    }
}
