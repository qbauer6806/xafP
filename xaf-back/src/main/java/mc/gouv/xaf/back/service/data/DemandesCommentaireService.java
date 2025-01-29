package mc.gouv.xaf.back.service.data;

import java.util.List;
import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;

public interface DemandesCommentaireService {

    List<DemandeCommentaireDTO> getCommentairesInternes(Integer demandeId);

    void putCommentaireInterne(Integer demandeId, DemandeCommentaireDTO commentaire);

}
