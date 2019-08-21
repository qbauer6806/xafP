package mc.gouv.af.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.pdf.recap.PdfRecapGenerationService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Classe service appelée par le process Activiti pour générer le fichier
 * interne de la récapitulation d'une demande au format PDF.
 * 
 * @author mboutelier.ext
 *
 */
@Component
public class GouvBPMDemandeRecapDelegate implements JavaDelegate {

	private static final Logger LOGGER = LoggerFactory.getLogger(GouvBPMDemandeRecapDelegate.class);

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemandesService demandesService;

	@Autowired
	private PdfRecapGenerationService pdfRecapGenerationService;

	@Override
	public void execute(DelegateExecution execution) throws Exception {

		LOGGER.info("==== AF-BACK DEMANDE RECAP SERVICE ...");

		DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
				Integer.parseInt(execution.getProcessBusinessKey()));

		pdfRecapGenerationService.generateAndStorePdf(demandeDto);

		LOGGER.info("==== AF-BACK DEMANDE RECAP SERVICE <fin>");
	}

}
