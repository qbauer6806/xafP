package mc.gouv.xaf.back.xls;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mc.gouv.xaf.back.service.excel.ExcelExportModelProvider;
import mc.gouv.xaf.back.service.excel.ExcelExportService;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire pour la classe ExcellExportService
 * @author dsaidiparto.ext
 *
 */
@Disabled
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class ExcelExportServiceTest {
	
	 @Autowired
	    private ExcelExportModelProvider excelExportModelProvider;
	 
	 @Autowired 
	 private ExcelExportService excelExportServie; 

	 /**
	  * Crée un fichier excel en utilisant un template demandes.xlsx
	  * @return
	  * @throws IOException
	  */
	 private byte[] createExcel() throws IOException{
		 byte[] result = null;
		 
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			excelExportServie.exportExcel("demandes.xlsx", excelExportModelProvider.getModel(null), out);
			result = out.toByteArray();
		}
		return result;
	 }
	 
	 /**
	  * Verifie que le fichier excel contient  le champs : identifiant et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	 @Test
	 public void shouldHaveCorrectIdentifier() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(), 0).matches(ExcelMockData.IDENTIFIANT));
	 }

	 /**
	  * Verifie que le fichier excel contient  le champs : " Date de création" et il se trouve dans un column correct  
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectDate() throws InvalidFormatException, IOException{
		String datestring = new SimpleDateFormat("dd/MM/yyyy").format(ExcelMockData.DATE_CREATION);
		assertTrue(getExcelCell(createExcel(), 1).matches(datestring));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs :  "dernier status" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectStatus() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(), 2).matches(ExcelMockData.DERNIER_STATUT));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : nom de usager et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectUsagerNom() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),3).matches(ExcelMockData.USAGER_NOM));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs :  prénom de usager et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectUsagerPreNom() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),4).matches(ExcelMockData.USAGER_PRENOM));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : email de usager et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectUsagerEmail() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),5).matches(ExcelMockData.USAGER_EMAIL));
	}
	
	 /**
	  * Verifie que le fichier excel contient le champs : nom  de aganet et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectAgentAffecteNom() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),6).matches(ExcelMockData.AGENT_AFFECT_NAME));
	}

	 /**
	  * Verifie que le fichier excel contient le champs : canal et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectCanal() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),7).matches(ExcelMockData.CANAL));
	}

	 /**
	  * Verifie que le fichier excel contient le champs : langue et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectLangue() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),8).matches(ExcelMockData.LANGUE));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : observation et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectObservation() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),9).matches(ExcelMockData.OBSERVATIONS));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Réference courrier interne" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectCourrierRefInterne() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),10).matches(ExcelMockData.COURRIER_REF_INTERNE));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : "Date de réception de courrier" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test
	public void shouldHaveCorrectCourrierCourrierDateDeModification() throws InvalidFormatException, IOException{
		String datestring = new SimpleDateFormat("dd/MM/yyyy").format(ExcelMockData.COURRIER_DATE_RECEPTION);
		System.out.println("Date Courrier : " + datestring);
		assertTrue(getExcelCell(createExcel(),11).matches(datestring));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Titre" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectTitre() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),12).matches(ExcelMockData.USAGER_TITRE));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "nom" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectUsagerNomAtPersonalPart() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),13).matches(ExcelMockData.USAGER_NOM));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Adress ligne 1" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectAddressLine1() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),14).matches(ExcelMockData.ADDRESS_LIGNE1));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Adress ligne 2" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectAddressLine2() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),15).matches(ExcelMockData.ADDRESS_LIGNE2));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Adress ligne 3" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectAddressLine3() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),16).matches(ExcelMockData.ADDRESS_LIGNE3));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Code postal" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPostalCode() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),17).matches(ExcelMockData.CODE_POSTAL));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Ville" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectCity() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),18).matches(ExcelMockData.VILLE));
	}
	 
	 /**
	  * Verifie que le fichier excel contient  le champs : "permis A" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisA() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),19).matches("TRUE"));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : " Permis A Cyclo" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisCyclo() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),20).matches("TRUE"));
	}
	 /**
	  * Verifie que le fichier excel contient  le champs : " Permis B" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisB() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),21).matches("TRUE"));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "permis C" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisC() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),22).matches("FALSE"));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "permis CE" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisCE() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),23).matches("FALSE"));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : "permis D" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisD() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),24).matches("FALSE"));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "permis DE" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPermisDE() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),25).matches("FALSE"));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "permis DE" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectPasDePermis() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),26).matches("FALSE"));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : "Information supplémentaire" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectSuplementaryInformation() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),27).matches(ExcelMockData.INFO_SUPPL));
	}

	 /**
	  * Verifie que le fichier excel contient  le champs : "Diplôme" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectDiploma() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),28).matches(ExcelMockData.DIPLOME));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Compétences" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectCompetence() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),29).matches(ExcelMockData.SKILLS));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Comment" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectComment() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),30).matches(ExcelMockData.COMMENT));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Fonction recherché" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectFonctionRecherchee() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),31).matches(ExcelMockData.VACANCY));
	}
	
	 /**
	  * Verifie que le fichier excel contient  le champs : "Langus" et il se trouve dans un column correct 
	  * @throws InvalidFormatException
	  * @throws IOException
	  */
	@Test 
	public void shouldHaveCorrectLanguages() throws InvalidFormatException, IOException{
		assertTrue(getExcelCell(createExcel(),32).matches(ExcelMockData.LANGUEUAGES));
	}

	
	private String getExcelCell(byte[] input, int cellIndex) throws InvalidFormatException, IOException {
	
		String cellContent = "";
		InputStream stream  = new ByteArrayInputStream(input);
		XSSFWorkbook workbook = new XSSFWorkbook(stream); 
		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row = sheet.getRow(2);
		XSSFCell cell = row.getCell(cellIndex);
		cellContent = cell.toString().trim();
		System.out.println(cell.getColumnIndex() + " :"+ cell.toString());
		workbook.close();
		return cellContent;
	}
}
