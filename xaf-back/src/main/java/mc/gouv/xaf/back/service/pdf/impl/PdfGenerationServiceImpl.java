package mc.gouv.xaf.back.service.pdf.impl;

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
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
import mc.gouv.xaf.back.service.pdf.PdfTemplateAndModelProvider;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.PdfTemplateAndModelDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map.Entry;

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
	private static final String APPEL_MESSAGE = "Appel au TemplateAndModelProvider de la démarche {}...";

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
	
	@Autowired
	private AfBackUtils afBackUtils;

	@Override
	public void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta) throws IOException {
		generateAndStorePdf(demande,pdfType, meta, generatePdf(demande, pdfType));
	}

	@Override
	public void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta, File tempFile) throws IOException {

		LOGGER.info("PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);

		String fileName = tempFile.getName();
		String url = fileService.sendToFile(tempFile, demande, fileName);

		// Ajout des données concernant le fichier généré aux métas
		meta += ";" + FileUtils.generateMetaData(tempFile);

		// Supprimer le fichier temporaire car il n'est plus utile
		LOGGER.info("Suppression du fichier temporaire...");
		try {
			Files.delete(Paths.get(tempFile.getPath()));
		} catch (IOException e) {
			LOGGER.warn("La suppression du fichier temporaire a échoué", e);
		}

		if (pdfType == PdfTypeEnum.FICHIER) {
			saveFichier(fileName, url, demande, meta);
		} else {
			saveCourrier(fileName, url, demande, meta);
		}

		LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);
	}

	private void saveCourrier(String fileName, String url, DemandeDTO demande, String meta) {
		LOGGER.info("Ajout de la référence à ce courrier dans DEM...");
		DemandeCourrierDTO courrier = new DemandeCourrierDTO();
		courrier.setName(fileName);
		courrier.setUrl(url);
		courrier.setMeta(meta);
		demandesCourriersService.saveCourrier(demande.getPkDemandes(), courrier);
	}

	@Override
	public void saveFichier(String fileName, String url, DemandeDTO demande, String meta) {
		LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
		DemandeFileDTO file = new DemandeFileDTO();
		file.setName(fileName);
		file.setUrl('/' + url);
		file.setDate(new Date());
		file.setMeta(meta);
		demandesFileService.saveFile(file, demande.getPkDemandes());
	}

	private File generatePdf(DemandeDTO demande, PdfTypeEnum pdfType) {
		LOGGER.info(APPEL_MESSAGE, gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider.getTemplateAndModel(demande, pdfType);
		return generateToFile(demande, dto);
	}

	@Override
	public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
			String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {

		LOGGER.info(APPEL_MESSAGE, gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider
				.getTemplateAndModelForPreview(demande, statutSuivant, codeMotif, langue, commentaire, texteAEnvoyer, pdfType);
		return generateToFile(demande, dto);
	}

	public byte[] generatePdfToStream(DemandeDTO demande) throws IOException, XDocReportException {
		LOGGER.info(APPEL_MESSAGE, gouvPropertiesResolver.getDemarcheId());
		PdfTemplateAndModelDTO dto = pdfTemplateAndModelProvider.getTemplateAndModel(demande, PdfTypeEnum.COURRIER);
		return generateToStream(dto);
	}

	@Override
	public File generateToFile(DemandeDTO demande, PdfTemplateAndModelDTO dto) {

		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = dto.getFilename() + AfBackUtils.generateFileDateSuffix() + ".pdf";
		File temp = new File(tempDir, fileName);

		try {
			try (OutputStream out = new FileOutputStream(temp)) {
				byte[] bytes = generateToStream(dto);
				out.write(bytes);
			}
		} catch (IOException | XDocReportException e) {
			LOGGER.error("Erreur lors de la génération PDF", e);
		}

		return temp;
	}

	private byte[] generateToStream(PdfTemplateAndModelDTO dto)
			throws IOException, XDocReportException {

		byte[] bytes;

		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			LOGGER.info("Chargement du template {} via appel à FILE...", dto.getTemplateFilename());
			// #16180 Ancienne façon : aller chercher dans src/main/resources... maintenant on cherche dans FILE
			InputStream in = afBackUtils.getFileClient().getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", dto.getTemplateFilename());
			IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

			LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
			IContext context = report.createContext();
			context.put("StringUtils", StringUtils.class);
			context.put("Utils", AfBackUtils.class);
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
