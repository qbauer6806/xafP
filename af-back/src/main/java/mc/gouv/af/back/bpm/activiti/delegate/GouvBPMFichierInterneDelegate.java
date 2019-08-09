package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.pdf.PdfGenerationService;
import mc.gouv.af.back.pdf.PdfType;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour générer le fichier
 * interne d'une demande au format PDF.
 * 
 * @author mboutelier.ext
 *
 */
@Component
public class GouvBPMFichierInterneDelegate implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMFichierInterneDelegate.class);

	@Autowired
	private PdfGenerationService pdfGenerationService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemandesService demandesService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		LOGGER.info("==== AF-BACK FICHIER INTERNE SERVICE ...");

		DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
				Integer.parseInt(execution.getProcessBusinessKey()));

		pdfGenerationService.generateAndStorePdf(demandeDto, PdfType.FICHIER_INTERNE);

		LOGGER.info("==== AF-BACK FICHIER INTERNE SERVICE <fin>");
	}

}
