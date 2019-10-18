package mc.gouv.xaf.back.xls;

import mc.gouv.xaf.back.DemandeContenuMockDTO;
import mc.gouv.dem.shared.model.DemandeFlatDTO;

public class DemandeExcelFlatMockDTO {

	    
	    public DemandeExcelFlatMockDTO(DemandeFlatDTO generic, DemandeContenuMockDTO contenu ) {
	        this.generic = generic;
	        this.contenu = contenu;
	    }

	    private DemandeFlatDTO generic;
	    
	    private DemandeContenuMockDTO contenu;
	    
	    private String situation;
	    
	    private String langues;
	    
	    private String assermentations;
	    
	    private String fonctionRecherchee;
	    
	    private String usagerTitre;
	    
	    public DemandeFlatDTO getGeneric() {
	        return generic;
	    }
	    
	    public void setGeneric(DemandeFlatDTO generic) {
	        this.generic = generic;
	    }

	    public void setContenu(DemandeContenuMockDTO contenu) {
	    	this.contenu = contenu;
	    }
	    
	    public DemandeContenuMockDTO getContenu() {
	    	return contenu;
	    }
	    
	    public String getSituation() {
	        return situation;
	    }
	    
	    public void setSituation(String situation) {
	        this.situation = situation;
	    }

	    public String getLangues() {
	        return langues;
	    }
	    
	    public void setLangues(String langues) {
	        this.langues = langues;
	    }

	    public String getAssermentations() {
	        return assermentations;
	    }
	    
	    public void setAssermentations(String assermentations) {
	        this.assermentations = assermentations;
	    }

	    public String getFonctionRecherchee() {
	        return fonctionRecherchee;
	    }

	    public void setFonctionRecherchee(String fonctionRecherchee) {
	        this.fonctionRecherchee = fonctionRecherchee;
	    }

		public String getUsagerTitre() {
			return usagerTitre;
		}

		public void setUsagerTitre(String usagerTitre) {
			this.usagerTitre = usagerTitre;
		}
		
	}	

