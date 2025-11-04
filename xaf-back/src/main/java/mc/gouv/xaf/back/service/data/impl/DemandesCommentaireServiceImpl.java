package mc.gouv.xaf.back.service.data.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesCommentaireRepository;
import mc.gouv.xaf.back.data.transformer.DemandesCommentaireTransformer;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesCommentaireServiceImpl implements DemandesCommentaireService {

    private final DemandesCommentaireRepository demandesCommentaireRepository;

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
