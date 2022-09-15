package mc.gouv.xaf.back.paiement.service.itg;

import mc.gouv.xaf.back.paiement.dto.MoyenPaiementDTO;
import mc.gouv.xaf.back.paiement.dto.OperationDTO;
import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.shared.dto.itg.monetico.MoneticoResponseDTO;

import java.util.List;

public interface MoneticoPaiementService {

    PaiementDTO create(String demandesId, String langue, Integer usagerId, boolean iframe);

    void updateStatus(MoneticoResponseDTO moneticoResponseDTO);

    MoyenPaiementDTO getMoyenPaiement(Integer demandeId);

    List<MoyenPaiementDTO> getAllMoyensPaiement();

    List<OperationDTO> getAllOperations();

}
