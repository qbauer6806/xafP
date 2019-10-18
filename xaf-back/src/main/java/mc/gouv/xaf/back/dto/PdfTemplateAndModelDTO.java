package mc.gouv.xaf.back.dto;

import java.util.Map;

public class PdfTemplateAndModelDTO {
	
	private String templateFilename;
	
	private String filename;
	
	private Map<String,Object> model;

	public String getTemplateFilename() {
		return templateFilename;
	}

	public void setTemplateFilename(String templateFilename) {
		this.templateFilename = templateFilename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}
	
}
