#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.service.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.docx4j.Docx4J;
import org.docx4j.fonts.PhysicalFonts;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.file.FileService;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesFilesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeFileDTO;
import mc.gouv.${artifactIdLower}.service.provider.PdfModelProvider;
import mc.gouv.${artifactIdLower}.shared.util.${artifactIdCamelCase}Utils;

@Component
public class PdfGeneratorService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfGeneratorService.class);

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");

    @Autowired
    private FileService fileService;

    @Autowired
    private DemandesFilesService demandesFilesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @PostConstruct
    public void init() {
        PhysicalFonts.setRegex(".*times.*");
    }

    public void saveFile(DemandeDTO demandeDto, PdfModelProvider provider)
            throws Exception {

        LOGGER.info("Génération du PDF de calcul de l'aide...");
        final String fileName = new StringBuilder().append("FORM_CGD_").append(demandeDto.getIdentifiant()).append("_")
                .append(dateFormat.format(new Date())).toString();

        File tempFile = generateFile(provider.xmlModelGenerator(demandeDto), fileName);

        LOGGER.info("Stockage du PDF généré dans FILE...");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String url = fileService.saveFile(demandeDto, tempFile.getName(), "application/pdf",
                new FileInputStream(tempFile),
                output);
        output.close();

        LOGGER.info("Ajout de la référence à ce fichier dans DEM...");
        DemandeFileDTO demandeFile = new DemandeFileDTO();
        demandeFile.setUrl("/" + url);
        demandeFile.setName(fileName + ".pdf");
        demandeFile.setMeta(${artifactIdCamelCase}Utils.${artifactIdUpper}_CALCULAIDE_FILE);
        demandeFile.setDate(new Date());
        demandesFilesService.saveFile(demandeFile, gouvPropertiesResolver.getDemarcheId(), demandeDto.getPkDemandes());
    }

    private File generateFile(String xmlDocument, String fileName) throws Docx4JException, IOException {
        InputStream template = new ClassPathResource("/pdf/Template_Demande_Generee_CGD.docx").getInputStream();

        WordprocessingMLPackage wordMLPackage = Docx4J.load(template);

        Docx4J.bind(wordMLPackage, xmlDocument,
                Docx4J.FLAG_BIND_INSERT_XML | Docx4J.FLAG_BIND_BIND_XML | Docx4J.FLAG_BIND_REMOVE_SDT);

        File file = File.createTempFile(fileName, ".pdf");
        OutputStream out = new FileOutputStream(file);
        Docx4J.toPDF(wordMLPackage, out);
        out.flush();
        out.close();

        return file;

    }

}
