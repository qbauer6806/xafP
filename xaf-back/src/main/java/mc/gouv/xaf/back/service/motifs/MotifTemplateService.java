package mc.gouv.xaf.back.service.motifs;

import java.io.IOException;
import java.util.List;

import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;

/**
 * Used to retrieve the Motifs populated with Velocity pattern
 */
public interface MotifTemplateService {

    MotifDTO getMotif(DemandeDTO demande, String codeMotif, String langue) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException;

    List<MotifDTO> getMotifs(DemandeDTO demande, String langue, String statut) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException;

    List<MotifDTO> populateMotifs(DemandeDTO demande, List<MotifDTO> motifList) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException;

    List<MotifDTO> getFilteredMotifs(DemandeDTO demande, String langue, List<String> codes) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, IOException;
}
