package mc.gouv.af.back.pdf.recap;

import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;

/**
 * Event Handler qui permet d'ajouter un entête et un pied de page au fichier de
 * récapitulation
 * 
 * @author mboutelier.ext
 */
public class HeaderFooterPageEvent implements IEventHandler {

	private static final float MARGIN_BOTTOM = 30f;

	private Image image;
	private Float height;
	private boolean header;

	public HeaderFooterPageEvent(Image image, boolean header) {
		this.image = image;
		image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
		this.height = image.getImageScaledHeight();
		this.header = header;

		// Ajout de la marge dans la taille de l'image pour éviter les problèmes
		// d'overlap entre le texte de la page et le footer
		if (!header) {
			this.height += MARGIN_BOTTOM;
		}
	}

	@Override
	public void handleEvent(Event event) {
		PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
		PdfDocument pdfDoc = docEvent.getDocument();
		PdfPage page = docEvent.getPage();
		PdfCanvas aboveCanvas = new PdfCanvas(page.newContentStreamBefore(), page.getResources(), pdfDoc);
		Rectangle area = header ? page.getPageSize() : new Rectangle(0, 0, PageSize.A4.getWidth(), height);
		Canvas canvas = new Canvas(aboveCanvas, pdfDoc, area);
		canvas.add(image);
		canvas.close();
	}

	public Float getHeight() {
		return height;
	}

}
