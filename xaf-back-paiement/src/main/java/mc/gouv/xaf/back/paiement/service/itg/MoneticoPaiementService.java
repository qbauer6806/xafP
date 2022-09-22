package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.CommandeDTO;
import mc.gouv.xaf.back.paiement.dto.CommandeOperationDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;

import java.util.List;

public interface MoneticoPaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe);

    String updateStatus(MoneticoResponseDTO moneticoResponseDTO);

    CommandeDTO getCommande(Integer demandeId);

    List<CommandeDTO> getAllCommandes();

    List<CommandeOperationDTO> getAllOperations();

}
