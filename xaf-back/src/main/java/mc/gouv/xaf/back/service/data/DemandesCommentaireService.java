package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.shared.dto.DemandeCommentaireDTO;

import java.util.List;

public interface DemandesCommentaireService {

    List<DemandeCommentaireDTO> getCommentairesInternes(Integer demandeId);

    void putCommentaireInterne(DemandeCommentaireDTO commentaire);

}
