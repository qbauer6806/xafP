package mc.gouv.xaf.shared.dto;

import java.util.List;

public class FileSubCategoryDTO {
	
	private String key;

	private String name;

	private List<DemandeFileDTO> files;

	private boolean typedoc;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DemandeFileDTO> getFiles() {
		return files;
	}

	public void setFiles(List<DemandeFileDTO> files) {
		this.files = files;
	}

	public boolean isTypedoc() {
		return typedoc;
	}

	public void setTypedoc(boolean typedoc) {
		this.typedoc = typedoc;
	}
}
