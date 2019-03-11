package mc.gouv.af.back.pdf;
import mc.gouv.dem.shared.model.DemandeDTO;
public class GenericDemandeDtoMockGenerator {

//	 private final String CURRENT_DATE = "06/03/2019";
//	 private final String ADDRESS = " 9 rue alber II , Monaco ";
//	 private final String CODEPOSTAL = "123456";
//	 private final String CITY = "Monte Carlo";
//	 private final String IDENTIFIER = "TestUserID";
//	 private final String DEPOSITE_DATE = "01/03/2019";
//	 private final String TITLE = "Mr";
//	 private final String FIRST_NAME = "Bob";
//	 private final String LAST_NAME = "TestMan";
//	 private final String MOTIF = "TestMotif";
//	 private final String REFERENCE = "Test Reference";
//	 private final String COMMENT = "Celui c'est un commentaire";
	 
	
	
	public static final DemandeDTO givenGenericMockDemandeDTO() {
		DemandeDTO dto;
		dto = new DemandeDTO();
		dto.setPkDemandes(1);
		return dto;
	}
	
}
