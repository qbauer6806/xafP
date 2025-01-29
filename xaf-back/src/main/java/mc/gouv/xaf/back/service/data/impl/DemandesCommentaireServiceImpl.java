package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesCommentaireRepository;
import mc.gouv.xaf.back.data.transformer.DemandesCommentaireTransformer;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesCommentaireServiceImpl implements DemandesCommentaireService {

    @Autowired
    private DemandesCommentaireRepository demandesCommentaireRepository;

    @Override
    public List<DemandeCommentaireDTO> getCommentairesInternes(Integer demandeId) {
        return DemandesCommentaireTransformer.bo2Dto(
                demandesCommentaireRepository.findByFkDemandesPkDemandesOrderByDateAsc(demandeId));
    }

    @Override
    public void putCommentaireInterne(DemandeCommentaireDTO commentaire) {
        demandesCommentaireRepository.save(DemandesCommentaireTransformer.dto2Bo(commentaire));
    }
}
