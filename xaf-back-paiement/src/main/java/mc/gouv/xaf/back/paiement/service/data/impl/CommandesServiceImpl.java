package mc.gouv.xaf.back.paiement.service.data.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.service.data.CommandesService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandesServiceImpl implements CommandesService {

    private final CommandeRepository commandeRepository;

    @Override
    public CommandeDTO getDerniereCommande(Integer demandeId) {
        List<CommandeBO> commandeBOs = commandeRepository.findByCommandesDemandes_Demande_PkDemandesOrderByDateCreationDesc(
                demandeId);
        return CommandeTransformer.bo2Dto(commandeBOs.getFirst());
    }

    @Override
    public List<CommandeDTO> getAllCommandes() {
        List<CommandeBO> commandes = commandeRepository.findAll();
        return CommandeTransformer.bos2Dtos(commandes);
    }

}
