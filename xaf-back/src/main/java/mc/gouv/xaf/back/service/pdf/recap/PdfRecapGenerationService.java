package mc.gouv.xaf.back.service.pdf.recap;

import java.io.File;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de
 * génération de PDF contenant un récapitulatif de la demande.
 * 
 * @author mboutelier.ext
 *
 */
public interface PdfRecapGenerationService {

	void generateAndStorePdf(DemandeDTO demande) throws Exception;

	File generatePdf(DemandeDTO demande) throws Exception;

}
