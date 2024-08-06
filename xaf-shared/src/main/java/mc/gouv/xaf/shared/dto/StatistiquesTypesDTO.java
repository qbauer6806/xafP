package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class StatistiquesTypesDTO {

	private Integer pkStatistiquesTypes;
	
	private String identifiantDemande;

    private String value;

}
