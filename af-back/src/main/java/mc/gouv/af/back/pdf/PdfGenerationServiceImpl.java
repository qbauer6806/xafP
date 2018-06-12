package mc.gouv.af.back.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import mc.gouv.af.back.file.FileService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesCourriersService;
import mc.gouv.dem.shared.model.DemandeCourrierDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Classe appelée par le workflow BPM, permettant d'appeler un sous-service de génération de PDF (implémenté
 * dans la démarche cible et de stocker le résultat de cette génération.
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
    
    @Override
    public void generateAndStorePdf(DemandeDTO demande) throws Exception {

        LOGGER.info("PdfGenerationServiceImpl.generateAndStorePdf(" + demande.getPkDemandes() + ")");

        LOGGER.info("Génération du PDF avec XDocReport...");        
        File tempFile = generatePdf(demande);
        

        LOGGER.info("Stockage du PDF généré dans FILE...");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String url = fileService.saveFile(demande, tempFile.getName(), "application/pdf", new FileInputStream(tempFile),
                output);
        output.close();

        LOGGER.info("Ajout de la référence à ce courrier dans DEM...");
        DemandeCourrierDTO courrier = new DemandeCourrierDTO();
        courrier.setName(tempFile.getName());
        courrier.setUrl(url);
        demandesCourriersService.saveCourrier(gouvPropertiesResolver.getDemarcheId(), demande.getPkDemandes(), courrier);

        LOGGER.info("Fin PdfGenerationServiceImpl.generateAndStorePdf(" + demande.getPkDemandes() + ")");
    }
    
    @Override
    public File generatePdf(DemandeDTO demande) {
        
        LOGGER.info("Appel au TemplateAndModelProvider de la démarche " + gouvPropertiesResolver.getDemarcheId() + "...");
        Entry<String, Map<String, Object>> templateAndModel = pdfTemplateAndModelProvider.getTemplateAndModel(demande);
        String templateFileName = templateAndModel.getKey();
        Map<String,Object> model = templateAndModel.getValue();
        
        return generate(demande, templateFileName, model);

    }
    
    @Override
    public File generatePdfPreview(DemandeDTO demande, String statutSuivant, String codeMotif, String langue,
            String commentaire) {
        
        LOGGER.info("Appel au TemplateAndModelProvider de la démarche " + gouvPropertiesResolver.getDemarcheId() + "...");
        Entry<String, Map<String, Object>> templateAndModel = pdfTemplateAndModelProvider.getTemplateAndModelForPreview(demande, statutSuivant,
                codeMotif, langue, commentaire);
        String templateFileName = templateAndModel.getKey();
        Map<String, Object> model = templateAndModel.getValue();
        
        return generate(demande, templateFileName, model);

    }
    
    private File generate(DemandeDTO demande, String templateFileName, Map<String, Object> model) {
        
        File temp = null;
        
        try {
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

            LOGGER.info("Génération du courrier PDF avec les template et modèle fournis...");
            temp = File.createTempFile("courrierDEM_pk" + demande.getPkDemandes().toString() + "_", ".pdf");
            OutputStream out = new FileOutputStream(temp);
            report.convert(context, options, out);

            out.close();

        } catch (IOException | XDocReportException e) {
            LOGGER.error("Erreur lors de la génération PDF", e);
        }

        return temp;
    }

}
