package mc.gouv.xaf.back.paiement.service.data.impl;

import mc.gouv.xaf.back.paiement.data.dao.CommandeRepository;
import mc.gouv.xaf.back.paiement.data.entity.CommandeBO;
import mc.gouv.xaf.back.paiement.data.transformer.CommandeTransformer;
import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.service.data.CommandesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommandesServiceImpl implements CommandesService {

    @Autowired
    private CommandeRepository commandeRepository;

    @Override
    public CommandeDTO getDerniereCommande(Integer demandeId) {
        List<CommandeBO> commandeBOs = commandeRepository.findByCommandesDemandes_Demande_PkDemandesOrderByDateCreationDesc(
                demandeId);
        return CommandeTransformer.bo2Dto(commandeBOs.get(0));
    }

    @Override
    public List<CommandeDTO> getAllCommandes() {
        List<CommandeBO> commandes = commandeRepository.findAll();
        return CommandeTransformer.bos2Dtos(commandes);
    }

}
