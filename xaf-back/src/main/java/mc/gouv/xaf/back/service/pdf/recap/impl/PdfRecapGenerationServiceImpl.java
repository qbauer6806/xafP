package mc.gouv.xaf.back.service.pdf.recap.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.recap.PdfHeaderFooterProvider;
import mc.gouv.xaf.back.service.pdf.recap.PdfRecapGenerationService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

@Component
public class PdfRecapGenerationServiceImpl implements PdfRecapGenerationService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfRecapGenerationServiceImpl.class);

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

        LOGGER.info("Génération du PDF avec Open HTML to PDF...");
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
        LOGGER.info("Récuppération des images pour le header et le footer...");
        File header = pdfHeaderFooterProvider.getHeader();
        File footer = pdfHeaderFooterProvider.getFooter();
        File htmlSource = generateHtmlSource(demande, header, footer);

        LOGGER.info("Conversion du code HTML en PDF...");
        File pdfDest = createTempFile("Demande_" + demande.getIdentifiant() + "_");

        try {
            OutputStream os = new FileOutputStream(pdfDest);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withFile(htmlSource);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la construction du fichier PDF: ", e);
            throw e;
        } finally {
            LOGGER.info("Suppression des fichiers temporaires...");
            if (null != htmlSource) {
                htmlSource.delete();
            }
            if (null != header) {
                header.delete();
            }
            if (null != footer) {
                footer.delete();
            }
        }

        LOGGER.info("Fin de la génération du PDF.");
        return pdfDest;
    }

    private File generateHtmlSource(DemandeDTO demande, File header, File footer) {
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
            String htmlRecap = demandeRecapHTMLService.getHTMLDemandeContenuRecap(demande, true);

            LOGGER.info("Création d'un fichier temporaire pour stocker le HTML...");
            htmlSource = File.createTempFile("tmpRecapHtml", ".html");
            PrintWriter writer = new PrintWriter(htmlSource);
            writer.println("<!DOCTYPE html><html><head>");

            LOGGER.info("Récupération de l'InputStream pour le fichier CSS pdfrecap/css/genpdf.css ...");
            InputStream fis = this.getClass().getResourceAsStream("/pdfrecap/css/genpdf.css");
            LOGGER.info("Largeur du fchier CSS à lire : {} bytes...", fis.available());
            writer.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
            writer.println("<style>");

            int content;
            while ((content = fis.read()) != -1) {
                // conversion en char avant écriture
                writer.print((char) content);
            }
            fis.close();

            writer.println("</style></head><body>");
            LOGGER.info("Fin de l'écriture du CSS...");

            writer.println("<div id=\"pageHeader\">");
            writer.print("<img src=\"");
            writer.print(header.toURI().getPath());
            writer.println("\" alt=\"HEADER\"></img>");
            writer.println("</div>");

            writer.println("<div id=\"pageFooter\">");
            writer.print("<img src=\"");
            writer.print(footer.toURI().getPath());
            writer.println("\" alt=\"FOOTER\"></img>");
            writer.println("</div>");

            LOGGER.info("Fin du header et footer...");

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

    private File createTempFile(String filename) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String fileName = filename + AfBackUtils.generateFileDateSuffix() + ".pdf";
        return new File(tempDir, fileName);
    }

}
