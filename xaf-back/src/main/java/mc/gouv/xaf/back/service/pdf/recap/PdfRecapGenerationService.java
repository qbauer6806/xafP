package mc.gouv.xaf.back.service.pdf.recap;

import java.io.File;
import java.io.IOException;

import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de
 * génération de PDF contenant un récapitulatif de la demande.
 * 
 * @author mboutelier.ext
 *
 */
public interface PdfRecapGenerationService {

	void generateAndStorePdf(DemandeDTO demande) throws IOException;

	File generatePdf(DemandeDTO demande);

}
