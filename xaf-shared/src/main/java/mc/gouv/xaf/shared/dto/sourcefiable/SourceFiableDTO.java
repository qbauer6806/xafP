package mc.gouv.xaf.shared.dto.sourcefiable;

import java.io.Serializable;

import mc.gouv.xaf.shared.dto.sourcefiable.enums.SourceFiablesEnum;

public class SourceFiableDTO implements Serializable {
	
	private static final long serialVersionUID = -7987341949488216363L;
	private String modelPath;
	private SourceFiablesEnum sourceFiable;
	//private String pictoUrl;
	
	public SourceFiableDTO() {
    }
	
	public SourceFiableDTO(String path, SourceFiablesEnum sourceFiable) {
		this.modelPath = path;
		this.sourceFiable = sourceFiable;
		//this.pictoUrl = pictoUrl;
    }
	
//	public String getPictoUrl() {
//		return pictoUrl;
//	}
//	public void setPictoUrl(String pictoUrl) {
//		this.pictoUrl = pictoUrl;
//	}
	public SourceFiablesEnum getSourceFiable() {
		return sourceFiable;
	}
	public void setSourceFiable(SourceFiablesEnum sourceFiable) {
		this.sourceFiable = sourceFiable;
	}
	public String getModelPath() {
		return modelPath;
	}
	public void setModelPath(String modelPath) {
		this.modelPath = modelPath;
	}
}
