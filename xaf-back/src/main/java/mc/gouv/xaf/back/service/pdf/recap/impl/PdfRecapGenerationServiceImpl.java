package mc.gouv.xaf.back.service.pdf.recap.impl;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import mc.gouv.xaf.back.exception.DemarchesServiceException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import com.openhtmltopdf.util.XRLog;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.recap.PdfHeaderFooterProvider;
import mc.gouv.xaf.back.service.pdf.recap.PdfRecapGenerationService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import static mc.gouv.xaf.back.service.utils.FileUtils.META_RECAP;

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
    public void generateAndStorePdf(DemandeDTO demande) throws IOException {
        LOGGER.info("RecapGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());

        LOGGER.info("Génération du PDF avec Open HTML to PDF...");
        File tempFile = generatePdf(demande);
        String fileName = tempFile.getName();
        String url = fileService.sendToFile(tempFile, demande, fileName);

        // Supprimer le fichier temporaire car il n'est plus utile
        LOGGER.info("Suppression du fichier temporaire...");
        try {
            Files.delete(Paths.get(tempFile.getPath()));
        } catch (IOException e) {
            LOGGER.warn("La suppression du fichier temporaire a échoué", e);
        }

        LOGGER.info("Vérification de l'existance d'un fichier récap...");
        List<DemandeFileDTO> files = demandesFileService.getFileByDemandeIdAndMeta(demande.getPkDemandes(), META_RECAP);

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
        file.setMeta(META_RECAP);
        file.setTypedoc(META_RECAP);
        demandesFileService.saveFile(file, gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());

        if (indexedDemandeService != null) {
            indexedDemandeService.indexDemande(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes());
        }

        LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf({})", demande.getPkDemandes());
    }

    @Override
    public File generatePdf(DemandeDTO demande) {
        LOGGER.info("Récuppération des images pour le header et le footer...");
        File header = pdfHeaderFooterProvider.getHeader();
        File footer = pdfHeaderFooterProvider.getFooter();
        File htmlSource = generateHtmlSource(demande, header, footer);

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
                String contenu = IOUtils.toString(inputStream);
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
                if (null != footer) {
                    Files.delete(Paths.get(footer.getPath()));
                }
            } catch (IOException e) {
                LOGGER.warn("La suppression des fichiers temporaires a échoué", e);
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
            try (PrintWriter writer = new PrintWriter(htmlSource)) {
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
	            if (null != header) {
	                writer.print("<img src=\"");
	                writer.print(header.toURI().getPath());
	                writer.println("\" alt=\"HEADER\"></img>");
	            }
	            writer.println("</div>");
	
	            writer.println("<div id=\"pageFooter\">");
	            if (null != footer) {
	                writer.print("<img src=\"");
	                writer.print(footer.toURI().getPath());
	                writer.println("\" alt=\"FOOTER\"></img>");
	            }
	            writer.println("</div>");
	
	            LOGGER.info("Fin du header et footer...");
	
	            writer.println("<h1>Récapitulatif de la demande</h1>");
	            writer.println("<h2>");
	            writer.println(afBackUtils.getDemarcheNom());
	            writer.println("</h2>");
	
	            writer.println("<table class=\"table-section sectiondemande\">");
	            writer.println("<tr><th class=\"table-section\">La Demande</th></tr><tr><td>");
	            writer.println(htmlDemande);
	            writer.println("</td></tr></table>");
	
	            writer.println("<table class=\"table-section sectionic\">");
	            writer.println("<tr><th class=\"table-section\">Informations Complémentaires</th></tr><tr><td>");
	            writer.println(htmlComp);
	            writer.println("</td></tr></table>");
	
	            writer.println("<table class=\"table-section sectionrecap\">");
	            writer.println("<tr><th class=\"table-section\">Demande Initiale</th></tr><tr><td>");
	            writer.println(htmlRecap);
	            writer.println("</td></tr></table>");
	
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
