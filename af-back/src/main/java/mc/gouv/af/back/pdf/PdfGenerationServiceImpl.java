package mc.gouv.af.back.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import mc.gouv.af.back.file.FileService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.dem.service.DemandesCourriersService;
import mc.gouv.dem.service.DemandesFilesService;
import mc.gouv.dem.shared.model.DemandeCourrierDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;

/**
 * 
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de
 * génération de PDF (implémenté dans la démarche cible et de stocker le
 * résultat de cette génération.
 * 
 * @author qdeme
 *
 */
@Component
public class PdfGenerationServiceImpl implements PdfGenerationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);

	@Autowired
	private PdfTemplateAndModelProvider pdfTemplateAndModelProvider;

	@Autowired
	private FileService fileService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private DemandesCourriersService demandesCourriersService;

	@Autowired
	private DemandesFilesService demandesFileService;

	@Autowired(required = false)
	private IndexedDemandeService indexedDemandeService;

	@Override
	public void generateAndStorePdf(DemandeDTO demande, PdfType pdfType) throws Exception {

		LOGGER.info("PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);

		LOGGER.info("Génération du PDF avec XDocReport...");
		File tempFile = generatePdf(demande, pdfType);
		String fileName = tempFile.getName();

		LOGGER.info("Stockage du PDF généré dans FILE...");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String url = fileService.saveFile(demande, fileName, "application/pdf", new FileInputStream(tempFile), output);
		output.close();

		if (pdfType == PdfType.FICHIER_INTERNE) {
			saveFichierInterne(fileName, url, demande);
		} else {
			saveCourrier(fileName, url, demande);
		}

		if (indexedDemandeService != null) {
			indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
		}

		LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);
	}

	private void saveCourrier(String fileName, String url, DemandeDTO demande) throws Exception {
		LOGGER.info("Ajout de la référence à ce courrier dans DEM...");
		DemandeCourrierDTO courrier = new DemandeCourrierDTO();
		courrier.setName(fileName);
		courrier.setUrl(url);
		demandesCourriersService.saveCourrier(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes(),
				courrier);
	}

	private void saveFichierInterne(String fileName, String url, DemandeDTO demande) throws Exception {
		LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
		DemandeFileDTO file = new DemandeFileDTO();
		file.setName(fileName);
		file.setUrl('/' + url);
		file.setDate(new Date());
		file.setMeta(PdfType.FICHIER_INTERNE.name());
		demandesFileService.saveFile(file, gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
	}

	@Override
	public File generatePdf(DemandeDTO demande, PdfType pdfType) {
		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		Entry<String, Map<String, Object>> templateAndModel = getTemplateAndModel(demande, pdfType);
		return generateToFile(demande, getTemplateFileName(templateAndModel), getModel(templateAndModel), pdfType);
	}

	@Override
	public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
			String commentaire, PdfType pdfType) {

		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		Entry<String, Map<String, Object>> templateAndModel = pdfTemplateAndModelProvider
				.getTemplateAndModelForPreview(demande, statutSuivant, codeMotif, langue, commentaire);
		return generateToFile(demande, getTemplateFileName(templateAndModel), getModel(templateAndModel), pdfType);
	}

	public byte[] generatePdfToStream(DemandeDTO demande, PdfType pdfType) throws IOException, XDocReportException {
		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		Entry<String, Map<String, Object>> templateAndModel = getTemplateAndModel(demande, pdfType);
		return generateToStream(demande, getTemplateFileName(templateAndModel), getModel(templateAndModel));
	}

	private Entry<String, Map<String, Object>> getTemplateAndModel(DemandeDTO demande, PdfType pdfType) {
		return (pdfType == PdfType.FICHIER_INTERNE)
				? pdfTemplateAndModelProvider.getFichierInterneTemplateAndModel(demande)
				: pdfTemplateAndModelProvider.getTemplateAndModel(demande);
	}

	private String getTemplateFileName(Entry<String, Map<String, Object>> templateAndModel) {
		return templateAndModel.getKey();
	}

	private Map<String, Object> getModel(Entry<String, Map<String, Object>> templateAndModel) {
		return templateAndModel.getValue();
	}

	private File generateToFile(DemandeDTO demande, String templateFileName, Map<String, Object> model,
			PdfType pdfType) {

		File temp = null;
		String fileName = buildFileName(pdfType, demande.getPkDemandes().toString());

		try {
			temp = File.createTempFile(fileName, ".pdf");
			try (OutputStream out = new FileOutputStream(temp)) {
				byte[] bytes = generateToStream(demande, templateFileName, model);
				out.write(bytes);
			}
		} catch (IOException | XDocReportException e) {
			LOGGER.error("Erreur lors de la génération PDF", e);
		}

		return temp;
	}

	private String buildFileName(PdfType pdfType, String pkString) {
		String fileType = (pdfType == PdfType.FICHIER_INTERNE) ? "fichierInterne" : "courrier";
		StringBuilder builder = new StringBuilder();
		builder.append(fileType);
		builder.append("DEM_pk");
		builder.append(pkString);
		builder.append('_');
		return builder.toString();
	}

	private byte[] generateToStream(DemandeDTO demande, String templateFileName, Map<String, Object> model)
			throws IOException, XDocReportException {

		byte[] bytes = null;

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			LOGGER.info("Chargement du template " + templateFileName + "...");
			InputStream in = new ClassPathResource("/pdf/" + templateFileName).getInputStream();
			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

			LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
			IContext context = report.createContext();
			context.put("StringUtils", StringUtils.class);
			for (Entry<String, Object> entry : model.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}

			Options options = Options.getTo(ConverterTypeTo.PDF);

			LOGGER.info("Récupération des PdfOptions...");
			PdfOptions pdfOptions = pdfTemplateAndModelProvider.getPdfOptions();
			if (pdfOptions != null) {
				options.subOptions(pdfOptions);
			}

			LOGGER.info("Génération du fichier PDF avec les template et modèle fournis...");
			report.convert(context, options, bos);
			bytes = bos.toByteArray();
		}

		return bytes;
	}

}
