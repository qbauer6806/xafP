package mc.gouv.xaf.shared.dto;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PdfTemplateAndModelDTO {
	
	private String templateFilename;
	
	private String filename;
	
	private Map<String,Object> model;

}
