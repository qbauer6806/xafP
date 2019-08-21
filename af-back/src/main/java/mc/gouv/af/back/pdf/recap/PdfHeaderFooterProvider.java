package mc.gouv.af.back.pdf.recap;

import com.itextpdf.layout.element.Image;

public interface PdfHeaderFooterProvider {
	
	Image getHeader();
	
	Image getFooter();

}
