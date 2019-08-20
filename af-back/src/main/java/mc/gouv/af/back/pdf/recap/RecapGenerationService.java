package mc.gouv.af.back.pdf.recap;

import java.io.File;

import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de
 * génération de PDF contenant un récapitulatif de la demande.
 * 
 * @author mboutelier.ext
 *
 */
public interface RecapGenerationService {

	public void generateAndStorePdf(DemandeDTO demande) throws Exception;

	public File generatePdf(DemandeDTO demande) throws Exception;

}
