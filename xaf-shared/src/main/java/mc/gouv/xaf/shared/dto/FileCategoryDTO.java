package mc.gouv.xaf.shared.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileCategoryDTO {

	private String name;

	private List<DemandeFileDTO> files;
	
	private List<FileSubCategoryDTO> subCategories;

	private boolean typedoc;

}
