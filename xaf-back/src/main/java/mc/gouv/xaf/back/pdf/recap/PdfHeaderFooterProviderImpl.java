package mc.gouv.xaf.back.pdf.recap;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

@Component
public class PdfHeaderFooterProviderImpl implements PdfHeaderFooterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfHeaderFooterProviderImpl.class);

	private static final String PATH = "/pdfrecap/img/";
	private static final String HEADER = "_Entete.jpg";
	private static final String FOOTER = "_Pied_de_page.jpg";

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	public Image getHeader() {
		Image img = null;
		try {
			String demarcheId = gouvPropertiesResolver.getDemarcheId();
			img = getImage(PATH + demarcheId + HEADER);
		} catch (MalformedURLException e) {
			LOGGER.error("Problème lors de la génération du header...", e);
		}
		return img;
	}

	public Image getFooter() {
		Image img = null;
		try {
			String demarcheId = gouvPropertiesResolver.getDemarcheId();
			img = getImage(PATH + demarcheId + FOOTER);
		} catch (MalformedURLException e) {
			LOGGER.error("Problème lors de la génération du footer...", e);
		}
		return img;
	}

	private Image getImage(String imgPath) throws MalformedURLException {
		URL path = this.getClass().getResource(imgPath);
		LOGGER.info("Chargement de l'image à l'adresse: {} ...", path.getPath());
		return new Image(ImageDataFactory.create(path));
	}
}
