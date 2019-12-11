package mc.gouv.xaf.backweb.formbean;

import javax.validation.constraints.NotNull;

import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Formulaire pour le preview des emails
 * 
 * @author mboutelier.ext
 *
 */
public class PdfPreviewFormBean extends PreviewFormBean {

	@NotNull
	private PdfTypeEnum pdfType;

	private String texteAEnvoyer;

	public PdfTypeEnum getPdfType() {
		return pdfType;
	}

	public void setPdfType(PdfTypeEnum pdfType) {
		this.pdfType = pdfType;
	}

	public String getTexteAEnvoyer() {
		return texteAEnvoyer;
	}

	public void setTexteAEnvoyer(String texteAEnvoyer) {
		this.texteAEnvoyer = texteAEnvoyer;
	}
}
