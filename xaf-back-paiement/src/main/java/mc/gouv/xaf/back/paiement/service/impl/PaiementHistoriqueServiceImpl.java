package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.data.dao.PaiementHistoriqueRepository;
import mc.gouv.xaf.back.paiement.data.entity.PaiementHistoriqueBO;
import mc.gouv.xaf.back.paiement.data.transformer.PaiementHistoriqueTransformer;
import mc.gouv.xaf.back.paiement.dto.PaiementHistoriqueDTO;
import mc.gouv.xaf.back.paiement.service.PaiementHistoriqueService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaiementHistoriqueServiceImpl implements PaiementHistoriqueService {

    @Autowired
    private PaiementHistoriqueRepository paiementHistoriqueRepository;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Override
    public List<PaiementHistoriqueDTO> findAllByDemandeId(Integer demandeId) {
        List<PaiementHistoriqueBO> bos = paiementHistoriqueRepository.findByFkDemandePkDemandes(demandeId);
        List<PaiementHistoriqueDTO> dtos = PaiementHistoriqueTransformer.bos2Dtos(bos);
        dtos.forEach(dto -> {
            StatutPublicOuInterneDTO statut = new StatutPublicOuInterneDTO();
            statut.setName(dto.getStatut().name());
            dto.setCouleur(demarchesDataProvider.getStatusColorClass(statut));
        });
        return dtos;
    }
}
