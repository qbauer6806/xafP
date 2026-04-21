package mc.gouv.xaf.back.service.pdf.impl;

import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
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
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.back.service.itg.file.FileService;
import mc.gouv.xaf.back.service.pdf.PdfGenerationService;
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
import org.springframework.stereotype.Component;

/**
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté dans la
 * démarche cible et de stocker le résultat de cette génération.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfGenerationServiceImpl.class);
    private static final String APPEL_MESSAGE = "Appel au TemplateAndModelProvider de la démarche {}...";

    private final AfPdfTemplateAndModelProvider afPdfTemplateAndModelProvider;
    private final FileService fileService;
    private final GouvPropertiesResolver gouvPropertiesResolver;
    private final DemandesCourriersService demandesCourriersService;
    private final DemandesFilesService demandesFileService;
    private final AfBackUtils afBackUtils;

    @Override
    public void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta) throws IOException {
        generateAndStorePdf(demande, pdfType, meta, true);
    }

    @Override
    public void generateAndStoreDoc(DemandeDTO demande, PdfTypeEnum pdfType, String meta,
            PdfTemplateAndModelDTO pdfTemplateAndModelDTO, boolean convertPdf) throws IOException {
        generateAndStorePdf(demande, pdfType, meta, convertPdf, pdfTemplateAndModelDTO);
    }

    private void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta, boolean convertPdf,
            PdfTemplateAndModelDTO pdfTemplateAndModelDTO) throws IOException {

        LOGGER.info("PdfGenerationServiceImpl.generateAndStorePdf({}, {})", demande.getPkDemandes(), pdfType);
        File tempFile = generateToFile(pdfTemplateAndModelDTO, convertPdf);

        String fileName = tempFile.getName();
        String url = fileService.sendToFile(tempFile, demande, fileName, convertPdf);

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

    private void generateAndStorePdf(DemandeDTO demande, PdfTypeEnum pdfType, String meta, boolean convertPdf)
            throws IOException {
        PdfTemplateAndModelDTO pdfTemplateAndModelDTO = afPdfTemplateAndModelProvider.getTemplateAndModel(demande,
                pdfType);
        generateAndStorePdf(demande, pdfType, meta, convertPdf, pdfTemplateAndModelDTO);
    }

    private void saveCourrier(String fileName, String url, DemandeDTO demande, String meta) {
        LOGGER.info("Ajout de la référence à ce courrier dans DEM...");
        DemandeCourrierDTO courrier = new DemandeCourrierDTO();
        courrier.setName(fileName);
        courrier.setUrl(url);
        courrier.setMeta(meta);
        demandesCourriersService.saveCourrier(demande.getPkDemandes(), courrier);
    }

    private void saveFichier(String fileName, String url, DemandeDTO demande, String meta) {
        LOGGER.info("Ajout de la référence à ce fichier interne dans DEM...");
        DemandeFileDTO file = new DemandeFileDTO();
        file.setName(fileName);
        file.setUrl('/' + url);
        file.setDate(new Date());
        file.setMeta(meta);
        demandesFileService.saveFile(file, demande.getPkDemandes());
    }

    @Override
    public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire, String texteAEnvoyer, PdfTypeEnum pdfType) {

        LOGGER.info(APPEL_MESSAGE, gouvPropertiesResolver.getDemarcheId());
        PdfTemplateAndModelDTO dto = afPdfTemplateAndModelProvider.getTemplateAndModelForPreview(demande, statutSuivant,
                codeMotif, langue, commentaire, texteAEnvoyer, pdfType);
        return generateToFile(dto, true);
    }

    @Override
    public File generateToFile(PdfTemplateAndModelDTO dto, boolean convertPdf) {

        String tempDir = System.getProperty("java.io.tmpdir");
        String extension = convertPdf ? ".pdf" : ".docx";
        String fileName = dto.getFilename() + AfBackUtils.generateFileDateSuffix() + extension;
        File temp = new File(tempDir, fileName);

        try {
            try (OutputStream out = new FileOutputStream(temp)) {
                byte[] bytes = generateToStream(dto, convertPdf);
                out.write(bytes);
            }
        } catch (IOException | XDocReportException e) {
            LOGGER.error("Erreur lors de la génération PDF", e);
        }

        return temp;
    }

    private byte[] generateToStream(PdfTemplateAndModelDTO dto, boolean convertPdf)
            throws IOException, XDocReportException {
        try (InputStream in = afBackUtils.getFileClient()
                .getFile(gouvPropertiesResolver.getDemarcheId(), "MODELES", dto.getTemplateFilename());
                ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            LOGGER.info("Chargement du template {} via appel à FILE...", dto.getTemplateFilename());
            IXDocReport report = XDocReportRegistry.getRegistry().loadReport(in, TemplateEngineKind.Velocity);

            LOGGER.info("Création du contexte avec le modèle fourni par la démarche...");
            IContext context = report.createContext();
            context.put("StringUtils", StringUtils.class);
            context.put("Utils", AfBackUtils.class);
            for (Entry<String, Object> entry : dto.getModel().entrySet()) {
                context.put(entry.getKey(), entry.getValue());
            }
            if (convertPdf) {
                Options options = Options.getTo(ConverterTypeTo.PDF);

                LOGGER.info("Récupération des PdfOptions...");
                PdfOptions pdfOptions = afPdfTemplateAndModelProvider.getPdfOptions();
                if (pdfOptions != null) {
                    options.subOptions(pdfOptions);
                }

                LOGGER.info("Génération du fichier PDF avec les template et modèle fournis...");
                report.convert(context, options, bos);
            } else {
                LOGGER.info("Remplissage du template DOCX...");
                report.process(context, bos);
            }

            return bos.toByteArray();
        }
    }

}
