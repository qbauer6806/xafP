package mc.gouv.xaf.back.service.pdf.recap.impl;

import mc.gouv.xaf.back.service.pdf.recap.PdfHeaderProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Component
public class PdfHeaderProviderImpl implements PdfHeaderProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfHeaderProviderImpl.class);


    @Override
    public File getHeader() {
        URL path = this.getClass().getResource("/pdfrecap/img/header-logo.jpg");
        LOGGER.info("Chargement de l'image à l'adresse: {} ...", path);
        File file = null;
        if (null != path) {
            try {
                BufferedImage img = ImageIO.read(path);
                file = File.createTempFile("header", ".jpg");
                ImageIO.write(img, "jpg", file);
            } catch (IOException e) {
                LOGGER.error("Problème lors de la récupération de l'image...", e);
            }
            if (null != file) {
                LOGGER.info("Fichier temporaire: {} ...", file.getPath());
            } else {
                LOGGER.info("Aucune image chargée à l'adresse: {} ...", path);
            }
        } else {
            LOGGER.error("Le chemin vers le fichier n'existe pas.");
        }
        return file;
    }

}
