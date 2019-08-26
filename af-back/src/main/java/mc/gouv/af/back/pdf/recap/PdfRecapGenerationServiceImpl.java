package mc.gouv.af.back.pdf.recap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Image;

import mc.gouv.af.back.file.FileService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.service.DemandeRecapHTMLService;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.af.back.util.AfBackUtils;
import mc.gouv.af.back.util.FileUtils;
import mc.gouv.dem.service.DemandesFilesService;
import mc.gouv.dem.shared.model.DemandeComplementsDTO;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;

@Component
public class PdfRecapGenerationServiceImpl implements PdfRecapGenerationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfRecapGenerationServiceImpl.class);

	private DateFormat dateFormat = new SimpleDateFormat("HHmm");

	@Autowired
	private FileService fileService;

	@Autowired
	private DemandesFilesService demandesFileService;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired(required = false)
	private IndexedDemandeService indexedDemandeService;

	@Autowired
	private DemandeRecapHTMLService demandeRecapHTMLService;

	@Autowired
	private PdfHeaderFooterProvider pdfHeaderFooterProvider;

	@Autowired
	private AfBackUtils afBackUtils;

	@Override
	public void generateAndStorePdf(DemandeDTO demande) throws Exception {
		LOGGER.info("RecapGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());

		LOGGER.info("Génération du PDF avec iText...");
		File tempFile = generatePdf(demande);
		String fileName = tempFile.getName();

		LOGGER.info("Stockage du PDF généré dans FILE...");
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		String url = fileService.saveFile(demande, fileName, "application/pdf", new FileInputStream(tempFile), output);
		output.close();

		LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
		DemandeFileDTO file = new DemandeFileDTO();
		file.setName(fileName);
		file.setUrl('/' + url);
		file.setDate(new Date());
		file.setMeta(FileUtils.META_BACK + "RECAP");
		demandesFileService.saveFile(file, gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());

		if (indexedDemandeService != null) {
			indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
		}

		LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());
	}

	@Override
	public File generatePdf(DemandeDTO demande) throws Exception {
		// Header
		Image header = pdfHeaderFooterProvider.getHeader();
		HeaderFooterPageEvent headerHandler = new HeaderFooterPageEvent(header, true);

		// Footer
		Image footer = pdfHeaderFooterProvider.getFooter();
		HeaderFooterPageEvent footerHandler = new HeaderFooterPageEvent(footer, false);

		File htmlSource = generateHtmlSource(demande, headerHandler.getHeight(), footerHandler.getHeight());

		LOGGER.info("Conversion du code HTML en PDF...");
		String fileName = "Demande_" + demande.getIdentifiant() + "_" + dateFormat.format(new Date());
		File pdfDest = File.createTempFile(fileName, ".pdf");
		PdfWriter writer = new PdfWriter(pdfDest);
		PdfDocument pdfDocument = new PdfDocument(writer);
		pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, headerHandler);
		pdfDocument.addEventHandler(PdfDocumentEvent.END_PAGE, footerHandler);

		LOGGER.info("Récupération du base URI...");
		URI baseURI = new ClassPathResource("/pdfrecap/css/").getURL().toURI();
//		URI baseURI = this.getClass().getResource("/pdfrecap/css/genpdf.css").toURI();
		LOGGER.info("baseURI = {}", baseURI.getPath());
		String baseURIPath = new File(baseURI).getPath();
//		String parent = file.getParent();
		LOGGER.info("path = {}", baseURIPath);
		ConverterProperties converterProperties = new ConverterProperties();
		converterProperties.setBaseUri(baseURIPath);

		LOGGER.info("Appel du HTML Converter...");
		HtmlConverter.convertToPdf(new FileInputStream(htmlSource), pdfDocument, converterProperties);

		LOGGER.info("Suppression du fichier temporaire...");
		htmlSource.delete();
		pdfDocument.close();

		LOGGER.info("Fin de la génération du PDF.");
		return pdfDest;
	}

	private File generateHtmlSource(DemandeDTO demande, Float headerHeight, Float footerHeight) {
		File htmlSource = null;

		try {
			LOGGER.info("Génération du code HTML de la demande intiale...");
			String htmlDemande = demandeRecapHTMLService.getHTMLDemandeGeneric(demande);

			LOGGER.info("Génération du code HTML des demandes d'informations complémentaires...");
			DemandeComplementsDTO[] complements = demande.getComplements();
			String htmlComp = null != complements && complements.length > 0
					? demandeRecapHTMLService.getHTMLDemandeComplements(demande)
					: "Aucune demande d'informations complémentaires.";

			LOGGER.info("Génération du code HTML de la récap...");
			String htmlRecap = demandeRecapHTMLService.getHTMLDemandeContenuRecap(demande);

			LOGGER.info("Création d'un fichier temporaire pour stocker le HTML...");
			htmlSource = File.createTempFile("tmpRecapHtml", ".html");
			PrintWriter writer = new PrintWriter(htmlSource);

			writer.println("<html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"genpdf.css\"></head><body>");

			// Ajout d'un style CSS sur les pages pour laisser de la place au header et
			// au footer
			writer.println("<style>@page { margin-top: ");
			writer.println(headerHeight.intValue());
			writer.println("px; margin-bottom: ");
			writer.println(footerHeight.intValue());
			writer.println("px; }</style>");

			writer.println("<h1>Récapitulatif de la demande</h1>");
			writer.println("<h2>");
			writer.println(afBackUtils.getDemarcheNom());
			writer.println("</h2>");

			writer.println("<table class=\"sectiondemande\"><tr><th>La Demande</th></tr><tr><td>");
			writer.println(htmlDemande);
			writer.println("</td></tr></table>");

			writer.println("<table class=\"sectionic\"><tr><th>Informations Complémentaires</th></tr><tr><td>");
			writer.println(htmlComp);
			writer.println("</td></tr></table>");

			writer.println("<table class=\"sectionrecap\"><tr><th>Demande Initiale</th></tr><tr><td>");
			writer.println(htmlRecap);
			writer.println("</td></tr></table>");

			writer.println("</body></html>");

			writer.close();

		} catch (Exception e) {
			LOGGER.error("Erreur lors de la génération du code HTML", e);
		}

		return htmlSource;
	}

}
