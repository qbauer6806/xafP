package mc.gouv.af.back.service;

import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.MotifDTO;

import java.util.List;

/**
 * Used to retrieve the Motifs populated with Velocity pattern
 */
public interface MotifTemplateService {

    public MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws Exception;

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws Exception;

    public List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws Exception;
}
