package mc.gouv.af.back.pdf.recap;

import java.net.URL;

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

    public String getHeaderPath() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        String path = getImagePath(PATH + demarcheId + HEADER).getPath();
        return path;
    }

    public String getFooterPath() {
        String demarcheId = gouvPropertiesResolver.getDemarcheId();
        String path = getImagePath(PATH + demarcheId + FOOTER).getPath();
        return path;
    }

    private URL getImagePath(String imgPath) {
        URL path = this.getClass().getResource(imgPath);
        LOGGER.info("Chargement de l'image à l'adresse: {} ...", path);
        return path;
    }

}
