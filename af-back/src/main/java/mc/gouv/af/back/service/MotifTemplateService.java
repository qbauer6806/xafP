package mc.gouv.af.back.service;

import mc.gouv.dem.shared.model.MotifDTO;

import java.util.List;
import java.util.Map;

/**
 * Used to retrieve the Motifs previously populated with Velocity pattern
 */
public interface MotifTemplateService {

    /**
     * Populate the Motif list with model variables
     */
    public List<MotifDTO> populatedMotifs(List<MotifDTO> motifsList, Map<String, Object> model) throws Exception;
}
