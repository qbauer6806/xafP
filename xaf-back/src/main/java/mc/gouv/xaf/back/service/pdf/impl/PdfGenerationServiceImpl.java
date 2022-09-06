package mc.gouv.xaf.back.service.pdf.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;

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
	
	@Autowired
	private AfBackUtils afBackUtils;

	@Override
	public void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta) throws Exception {

		LOGGER.info("PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);

		LOGGER.info("Génération du PDF avec XDocReport...");
		File tempFile = generatePdf(demande, pdfType);
		String fileName = tempFile.getName();

		LOGGER.info("Stockage du PDF généré dans FILE...");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		FileInputStream fis = new FileInputStream(tempFile);
		String url = fileService.saveFile(demande, fileName, gouvPropertiesResolver.getContainerId(), "application/pdf", fis, output);
		output.close();
		fis.close();
		
		// Supprimer le fichier temporaire car il n'est plus utile
		LOGGER.info("Suppression du fichier temporaire...");
		if (!tempFile.delete()) {
			LOGGER.warn("La suppression du fichier temporaire a échoué");
		}

		if (pdfType == PdfTypeEnum.FICHIER) {
			saveFichier(fileName, url, demande, meta);
		} else {
			saveCourrier(fileName, url, demande, meta);
		}

		if (indexedDemandeService != null) {
			indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
		}

		LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);
	}

	private void saveCourrier(String fileName, String url, DemandeDTO demande, String meta) throws Exception {
		LOGGER.info("Ajout de la référence à ce courrier dans DEM...");
		DemandeCourrierDTO courrier = new DemandeCourrierDTO();
		courrier.setName(fileName);
		courrier.setUrl(url);
		courrier.setMeta(meta);
		demandesCourriersService.saveCourrier(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes(),
				courrier);
	}

	private void saveFichier(String fileName, String url, DemandeDTO demande, String meta) throws Exception {
		LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
		DemandeFileDTO file = new DemandeFileDTO();
		file.setName(fileName);
		file.setUrl('/' + url);
		file.setDate(new Date());
		file.setMeta(meta);
		demandesFileService.saveFile(file, gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
	}

	private File generatePdf(DemandeDTO demande, PdfTypeEnum pdfType) {
		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider.getTemplateAndModel(demande, pdfType);
		return generateToFile(demande, dto);
	}

	@Override
	public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
			String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {

		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider
				.getTemplateAndModelForPreview(demande, statutSuivant, codeMotif, langue, commentaire, texteAEnvoyer, pdfType);
		return generateToFile(demande, dto);
	}

	public byte[] generatePdfToStream(DemandeDTO demande) throws IOException, XDocReportException {
		LOGGER.info("Appel au TemplateAndModelProvider de la démarche {}...", gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider.getTemplateAndModel(demande, PdfTypeEnum.COURRIER);
		return generateToStream(demande, dto);
	}

	private File generateToFile(DemandeDTO demande, PdfTemplateAndModelDTO dto) {

		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = dto.getFilename() + afBackUtils.generateFileDateSuffix() + ".pdf";
		File temp = new File(tempDir, fileName);

		try {
			try (OutputStream out = new FileOutputStream(temp)) {
				byte[] bytes = generateToStream(demande, dto);
				out.write(bytes);
			}
		} catch (IOException | XDocReportException e) {
			LOGGER.error("Erreur lors de la génération PDF", e);
		}

		return temp;
	}

	private byte[] generateToStream(DemandeDTO demande, PdfTemplateAndModelDTO dto)
			throws IOException, XDocReportException {

		byte[] bytes = null;

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			LOGGER.info("Chargement du template {} via appel à FILE...", dto.getTemplateFilename());
			// #16180 Ancienne façon : aller chercher dans src/main/resources... maintenant on cherche dans FILE
			//InputStream in = new ClassPathResource("/pdf/" + dto.getTemplateFilename()).getInputStream();
			InputStream in = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", dto.getTemplateFilename());
			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

			LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
			IContext context = report.createContext();
			context.put("StringUtils", StringUtils.class);
			for (Entry<String, Object> entry : dto.getModel().entrySet()) {
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
			in.close();
		}

		return bytes;
	}

}
