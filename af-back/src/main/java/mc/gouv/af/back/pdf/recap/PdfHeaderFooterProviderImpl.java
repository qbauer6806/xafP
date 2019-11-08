package mc.gouv.af.back.pdf.recap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.properties.GouvPropertiesResolver;

@Component
public class PdfHeaderFooterProviderImpl implements PdfHeaderFooterProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfHeaderFooterProviderImpl.class);

    private static final String PATH = "/pdfrecap/img/";
    private static final String HEADER = "_Entete.jpg";
    private static final String FOOTER = "_Pied_de_page.jpg";

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public File getHeader() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        return getImage(PATH + demarcheId + HEADER, "header");
    }

    @Override
    public File getFooter() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        return getImage(PATH + demarcheId + FOOTER, "footer");
    }

    private File getImage(String imgPath, String tempName) {
        URL path = this.getClass().getResource(imgPath);
        LOGGER.info("Chargement de l'image à l'adresse: {} ...", path);
        File file = null;
        try {
            BufferedImage img = ImageIO.read(path);
            file = File.createTempFile(tempName, ".jpg");
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            LOGGER.error("Problème lors de la récuppération de l'image...", e);
        }
        if (null != file) {
            LOGGER.info("Fichier temporaire: {} ...", file.getPath());
        } else {
            LOGGER.info("Aucune image chargée à l'adresse: {} ...", path);
        }
        return file;
    }
}
