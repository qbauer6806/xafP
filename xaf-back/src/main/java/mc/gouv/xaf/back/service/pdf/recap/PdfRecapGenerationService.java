package mc.gouv.xaf.back.service.pdf.recap;

import java.io.File;

import mc.gouv.xaf.back.shared.dto.DemandeDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de
 * génération de PDF contenant un récapitulatif de la demande.
 * 
 * @author mboutelier.ext
 *
 */
public interface PdfRecapGenerationService {

	public void generateAndStorePdf(DemandeDTO demande) throws Exception;

	public File generatePdf(DemandeDTO demande) throws Exception;

}
