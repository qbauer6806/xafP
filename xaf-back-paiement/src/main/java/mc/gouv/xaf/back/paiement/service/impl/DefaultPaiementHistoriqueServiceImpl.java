package mc.gouv.xaf.back.paiement.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import mc.gouv.xaf.back.paiement.data.transformer.PaiementHistoriqueTransformer;
import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public abstract class DefaultPaiementHistoriqueServiceImpl implements PaiementHistoriqueService {

    private final PaiementHistoriqueRepository paiementHistoriqueRepository;

    private final AfBackUtils afBackUtils;

    @Override
    public List<PaiementHistoriqueDTO> findAllByDemandeId(Integer demandeId) {
        List<PaiementHistoriqueBO> bos = paiementHistoriqueRepository.findByFkDemandesPkDemandesOrderByDateDesc(
                demandeId);
        List<PaiementHistoriqueDTO> dtos = PaiementHistoriqueTransformer.bos2Dtos(bos);
        dtos.forEach(dto -> dto.setCouleur(afBackUtils.getStatusColorClass(dto.getStatut().name())));
        return dtos;
    }

    @Override
    public void ajouterHistorique(PaiementHistoriqueDTO dto) {
        PaiementHistoriqueBO bo = PaiementHistoriqueTransformer.dto2Bo(dto);
        paiementHistoriqueRepository.save(bo);
    }
}
