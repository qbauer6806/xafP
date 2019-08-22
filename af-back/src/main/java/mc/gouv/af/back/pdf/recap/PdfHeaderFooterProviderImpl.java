package mc.gouv.af.back.pdf.recap;

import java.net.MalformedURLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.Image;

import mc.gouv.af.back.properties.GouvPropertiesResolver;

@Component
public class PdfHeaderFooterProviderImpl implements PdfHeaderFooterProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfHeaderFooterProviderImpl.class);

	private static final String PATH = "pdfrecap/img/";
	private static final String HEADER = "_Entete.jpg";
	private static final String FOOTER = "_Pied_de_page.jpg";
	
	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	public Image getHeader() {
		Image img = null;
		try {
			String demarcheId = gouvPropertiesResolver.getDemarcheId();
			img = new Image(ImageDataFactory
					.create(PdfHeaderFooterProviderImpl.class.getClassLoader().getResource(PATH + demarcheId + HEADER).getPath()));
		} catch (MalformedURLException e) {
			LOGGER.error("Problème lors de la génération du header...", e);
		}
		return img;
	}

	public Image getFooter() {
		Image img = null;
		try {
			String demarcheId = gouvPropertiesResolver.getDemarcheId();
			img = new Image(ImageDataFactory
					.create(PdfHeaderFooterProviderImpl.class.getClassLoader().getResource(PATH + demarcheId + FOOTER).getPath()));
		} catch (MalformedURLException e) {
			LOGGER.error("Problème lors de la génération du footer...", e);
		}
		return img;
	}

}
