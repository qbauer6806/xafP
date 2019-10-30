package mc.gouv.xaf.back.service.motifs;

import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * Used to retrieve the Motifs populated with Velocity pattern
 */
public interface MotifTemplateService {

    MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws Exception;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws Exception;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws Exception;

    List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) throws Exception;
}
