package mc.gouv.xaf.back.paiement.service.data.impl;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.paiement.data.dao.CommandeDemandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeDemandeBO;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeDemandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDemandeDTO;
import mc.gouv.xaf.back.paiement.service.data.CommandesDemandesService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandesDemandesServiceImpl implements CommandesDemandesService {

    private final CommandeDemandeRepository commandeDemandeRepository;

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
