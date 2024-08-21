package mc.gouv.xaf.back.service.motifs;

import java.io.IOException;
import java.util.List;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * Used to retrieve the Motifs populated with a template engine pattern (e.g., Thymeleaf)
 */
public interface MotifTemplateService {

    MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws IOException;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws IOException;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws IOException;

    List<MotifDTO> populateMotifs(DemandeDTO demande, List<MotifDTO> motifList) throws IOException;

    List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) throws IOException;
}
