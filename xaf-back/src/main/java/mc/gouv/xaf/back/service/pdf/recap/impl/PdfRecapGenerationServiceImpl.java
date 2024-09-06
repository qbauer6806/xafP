package mc.gouv.xaf.back.service.pdf.recap.impl;

import static mc.gouv.xaf.back.service.utils.FileUtils.META_RECAP;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.recap.PdfHeaderProvider;
import mc.gouv.xaf.back.service.pdf.recap.PdfRecapGenerationService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class PdfRecapGenerationServiceImpl implements PdfRecapGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfRecapGenerationServiceImpl.class);
    
    private static final String SPAN_END_TAG = "</span>";
    
    private static final String TD_TR_TABLE_TAG = "</td></tr></table>";

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesFilesService demandesFileService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemandeRecapHTMLService demandeRecapHTMLService;

    @Autowired
    private PdfHeaderProvider pdfHeaderProvider;

    @Autowired
    private AfBackUtils afBackUtils;

    @Override
    public void generateAndStorePdf(DemandeDTO demande) throws IOException {
        generateAndStorePdf(demande, generatePdf(demande));
    }

    @Override
    public void generateAndStorePdf(DemandeDTO demande, File tempFile) throws IOException {
        LOGGER.info("RecapGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());

        LOGGER.info("Génération du PDF avec Open HTML to PDF...");
        String fileName = tempFile.getName();
        String url = fileService.sendToFile(tempFile, demande, fileName);
        String metas = META_RECAP + ";" + FileUtils.generateMetaData(tempFile);

        // Supprimer le fichier temporaire car il n'est plus utile
        LOGGER.info("Suppression du fichier temporaire...");
        try {
            Files.delete(Paths.get(tempFile.getPath()));
        } catch (IOException e) {
            LOGGER.warn("La suppression du fichier temporaire a échoué", e);
        }

        LOGGER.info("Vérification de l'existance d'un fichier récap...");
        List<DemandeFileDTO> files = demandesFileService.getFileByDemandeIdAndTypedoc(demande.getPkDemandes(), META_RECAP);

        DemandeFileDTO file = new DemandeFileDTO();
        if (!files.isEmpty()) {
            file = files.get(0);
            LOGGER.info("Suppression de l'ancien fichier dans FILES...");
            String urlASuppr = URLEncoder.encode(file.getUrl(), StandardCharsets.UTF_8);
            fileService.deleteFile("ROOT", urlASuppr);
        }

        LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
        file.setName(fileName);
        file.setUrl('/' + url);
        file.setDate(new Date());
        file.setMeta( metas);
        file.setTypedoc(META_RECAP);
        demandesFileService.saveFile(file, demande.getPkDemandes());

        LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());

    }

    @Override
    public File generatePdf(DemandeDTO demande) {
        return generatePdf(demande, null);
    }

    public File generatePdf(DemandeDTO demande, String extraContent) {
        LOGGER.info("Récupération des images pour le header et le footer...");
        File header = pdfHeaderProvider.getHeader();
        File htmlSource = generateHtmlSource(demande, header, extraContent);

        LOGGER.info("Conversion du code HTML en PDF...");
        File pdfDest = createTempFile("Demande_" + demande.getIdentifiant() + "_");

        LOGGER.info("Open HTML To PDF setup du logger");
        XRLog.setLoggingEnabled(true);
        XRLog.setLoggerImpl(new Slf4jLogger());

        try (OutputStream os = new FileOutputStream(pdfDest)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useSVGDrawer(new BatikSVGDrawer());
            try(FileInputStream inputStream = new FileInputStream(htmlSource)) {
                String contenu = IOUtils.toString(inputStream, Charset.defaultCharset());
                LOGGER.info("HTML Source : {}", contenu);
            }
            builder.withFile(htmlSource);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la construction du fichier PDF: {}", e.getMessage());
            throw new DemarchesServiceException("Erreur lors de la construction du fichier PDF", HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            LOGGER.info("Suppression des fichiers temporaires...");
            try {
                if (null != htmlSource) {
                    Files.delete(Paths.get(htmlSource.getPath()));
                }
                if (null != header) {
                    Files.delete(Paths.get(header.getPath()));
                }
            } catch (IOException e) {
                LOGGER.warn("La suppression des fichiers temporaires a échoué", e);
            }
        }

        LOGGER.info("Fin de la génération du PDF.");
        return pdfDest;
    }

    private File generateHtmlSource(DemandeDTO demande, File header, String extraContent) {
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

            DemarcheDTO demarche = afBackUtils.getDemarcheInfos();
            try (PrintWriter writer = new PrintWriter(htmlSource)) {
	            writer.println("<!DOCTYPE html><html><head>");

                writer.println("<meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
	            writer.println("<style>");

                // pageOrientation
                try (InputStream pageOrientation = this.getClass().getResourceAsStream("/pdfrecap/css/page" + afBackUtils.getRecapOrientation() +"-genpdf.css")) {
                    int content;
                    while ((content = pageOrientation.read()) != -1) {
                        // conversion en char avant écriture
                        writer.print((char) content);
                    }
                }
                // genpdf
                try (InputStream genpdf = this.getClass().getResourceAsStream("/pdfrecap/css/genpdf.css")) {
                    int content;
                    while ((content = genpdf.read()) != -1) {
                        // conversion en char avant écriture
                        writer.print((char) content);
                    }
                }
	
	            writer.println("</style></head><body>");
	            LOGGER.info("Fin de l'écriture du CSS...");
	
	            writer.println("<div id=\"pageHeader\">");
	            if (null != header) {
	                writer.print("<img id=\"imgLogo\" src=\"");
	                writer.print(header.toURI().getPath());
	                writer.println("\" alt=\"HEADER\"></img>");
	            }
                writer.println("<span id=\"nomDirection\">");
                writer.println(StringEscapeUtils.escapeXml(demarche.getNomDirection()));
                writer.println(SPAN_END_TAG);
                writer.println("<br/>");
                writer.println("<span id=\"nomSousDirection\">");
                writer.println(StringEscapeUtils.escapeXml(demarche.getNomSousDirection()));
                writer.println(SPAN_END_TAG);
	            writer.println("</div>");
	
	            writer.println("<div id=\"pageFooter\">");
                writer.println("<span id=\"adresseService\">");
                writer.println(demarche.getAdresseService());
                writer.println("<br/>");
                writer.println(demarche.getTelephoneService());
                writer.println(SPAN_END_TAG);
                writer.println("<span id=\"nomFooter\">");
                writer.println(StringEscapeUtils.escapeXml(demarche.getNomFooter()));
                writer.println(SPAN_END_TAG);
	            writer.println("</div>");
	
	            LOGGER.info("Fin du header et footer...");
	
	            writer.println("<h1>Récapitulatif de la demande</h1>");
	            writer.println("<h2>");
                writer.println(StringEscapeUtils.escapeXml(afBackUtils.getDemarcheNom()));
	            writer.println("</h2>");
	
	            writer.println("<table class=\"table-section sectiondemande\">");
	            writer.println("<tr><th class=\"table-section\">La Demande</th></tr><tr><td>");
	            writer.println(htmlDemande);
	            writer.println(TD_TR_TABLE_TAG);
	
	            writer.println("<table class=\"table-section sectionic\">");
	            writer.println("<tr><th class=\"table-section\">Informations Complémentaires</th></tr><tr><td>");
	            writer.println(htmlComp);
	            writer.println(TD_TR_TABLE_TAG);
	
	            writer.println("<table class=\"table-section sectionrecap\">");
	            writer.println("<tr><th class=\"table-section\">Demande Initiale</th></tr><tr><td>");
	            writer.println(htmlRecap);
	            writer.println(TD_TR_TABLE_TAG);

                if(extraContent != null) {
                    writer.println(extraContent);
                }
	
	            writer.println("</body></html>");
            }

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
