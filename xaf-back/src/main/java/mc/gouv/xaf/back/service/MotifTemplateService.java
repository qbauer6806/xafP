package mc.gouv.xaf.back.service;

import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.MotifDTO;

import java.util.List;

/**
 * Used to retrieve the Motifs populated with Velocity pattern
 */
public interface MotifTemplateService {

    MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws Exception;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws Exception;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws Exception;

    List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) throws Exception;
}
