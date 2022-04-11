package mc.gouv.xaf.shared.dto;

public class StatistiquesTypesDTO {
	
	public StatistiquesTypesDTO(String identifiantDemande, String value) {
		this.identifiantDemande = identifiantDemande;
		this.value = value;
	}
	
	public StatistiquesTypesDTO() {
		
	}
	
	private Integer pkStatistiquesTypes;
	
	private String identifiantDemande;

    private String value;
	
    public Integer getPkStatistiquesTypes() {
		return pkStatistiquesTypes;
	}

	public void setPkStatistiquesTypes(Integer pkStatistiquesTypes) {
		this.pkStatistiquesTypes = pkStatistiquesTypes;
	}

	public String getIdentifiantDemande() {
		return identifiantDemande;
	}

	public void setIdentifiantDemande(String identifiantDemande) {
		this.identifiantDemande = identifiantDemande;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	

}
