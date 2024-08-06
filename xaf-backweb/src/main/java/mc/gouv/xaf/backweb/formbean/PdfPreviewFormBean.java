package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;

/**
 * Formulaire pour le preview des emails
 * 
 * @author mboutelier.ext
 *
 */
@Setter
@Getter
public class PdfPreviewFormBean extends PreviewFormBean {

	@NotNull
	private PdfTypeEnum pdfType;

	private String texteAEnvoyer;

}
