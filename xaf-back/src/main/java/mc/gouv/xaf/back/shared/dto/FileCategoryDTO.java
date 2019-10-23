package mc.gouv.xaf.back.shared.dto;

import java.util.List;

public class FileCategoryDTO {

	private String name;
	
	private List<DemandeFileDTO> files;

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
	
}
