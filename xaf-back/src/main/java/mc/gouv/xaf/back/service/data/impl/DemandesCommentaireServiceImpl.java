package mc.gouv.xaf.back.service.data.impl;

import java.util.List;
import mc.gouv.xaf.back.data.dao.DemandesCommentaireRepository;
import mc.gouv.xaf.back.data.transformer.DemandesCommentaireTransformer;
import mc.gouv.xaf.back.service.data.DemandesCommentaireService;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesCommentaireServiceImpl implements DemandesCommentaireService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCommentaireServiceImpl.class);

    @Autowired
    private DemandesCommentaireRepository demandesCommentaireRepository;

    @Override
    public List<DemandeCommentaireDTO> getCommentairesInternes(Integer demandeId) {
        return DemandesCommentaireTransformer.bo2Dto(demandesCommentaireRepository.findByFkDemandesPkDemandes(demandeId));
    }

    @Override
    public void putCommentaireInterne(Integer demandeId, DemandeCommentaireDTO commentaire) {
        demandesCommentaireRepository.save(DemandesCommentaireTransformer.dto2Bo(commentaire));
    }
}
