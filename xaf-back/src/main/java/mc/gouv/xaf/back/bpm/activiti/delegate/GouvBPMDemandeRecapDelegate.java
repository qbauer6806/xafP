package mc.gouv.xaf.back.bpm.activiti.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.pdf.recap.PdfRecapGenerationService;
import mc.gouv.xaf.shared.dto.DemandeDTO;

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

		LOGGER.info("==== xaf-back DEMANDE RECAP SERVICE ...");

		DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
				Integer.parseInt(execution.getProcessBusinessKey()));

		pdfRecapGenerationService.generateAndStorePdf(demandeDto);

		LOGGER.info("==== xaf-back DEMANDE RECAP SERVICE <fin>");
	}

}
