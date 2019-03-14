package mc.gouv.af.back.pdf;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;

import fr.opensagres.xdocreport.core.XDocReportException;
import mc.gouv.af.back.AfBackServiceTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=AfBackServiceTestConfiguration.class)
public class PdfGenerationTest {
	
	private final String CHECK_TIITLE_FAMILY_PATTERN = "(\\w*) (\\w*) (\\w*) qui aura lieu le";
	private final String CHECK_IDENTIFIER_PATTERN = "Objet : Votre demande (\\w*)";
	private final String CHECK_REFERENCE_PATTERN = "Reference : (\\w*)";
	private final String CHECK_MOTIF_PATTERN = "Motif : (\\w*)";
	private final String CHECK_DATEDEPOT_PATTERN = "Date de déposition : (\\d{2}/\\d{2}/\\d{4})";
	private final String CHECK_CURRENT_DATE_PATTERN = "Monaco, le (\\d{2}/\\d{2}/\\d{4})";
	private final String CHECK_BEGIN_DATE_PATTERN = "lieu le (\\d{2}/\\d{2}/\\d{4})";
	private final String CHECK_END_DATE_PATTERN = "au (\\d{2}/\\d{2}/\\d{4})";
	private final String CHECK_BLANK_FIELD_ADJASMENT_PATTERN = "_RAPPELLE_\\n_FIN_RAPPELLE_";
	
	@Autowired
	private PdfGenerationServiceImpl pdfGenerationService;
	
    @Autowired
    ApplicationContext applicationContext;
    
    private String extractPdfText() throws IOException, XDocReportException {

    	byte[] bytes = null;
    	StringBuilder content = new StringBuilder();
   	 
		bytes = pdfGenerationService.generatePdfToStream(GenericDemandeDtoMockGenerator.givenGenericMockDemandeDTO());
   	
			PdfReader reader = new PdfReader(bytes);
			PdfTextExtractor extractor = new PdfTextExtractor(reader);
            
			for (int i = 1; i <= reader.getNumberOfPages(); i++){
                 content.append(extractor.getTextFromPage(i));
            }
            
    	
    	return content.toString();
    }
    
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindCorrectUserIdentifier() throws IOException, XDocReportException {
		Pattern pattern =  Pattern.compile(CHECK_IDENTIFIER_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.IDENTIFIER.equals(matcher.group(1)));
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindTittleFirstNameLastName () throws IOException, XDocReportException{
		Pattern pattern =  Pattern.compile(CHECK_TIITLE_FAMILY_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.TITLE.equals(matcher.group(1)));
		assertTrue(PDFServiceConstantsMock.FIRST_NAME.equals(matcher.group(2)));
		assertTrue(PDFServiceConstantsMock.LAST_NAME.equals(matcher.group(3)));
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindCurrentDateField()  throws IOException, XDocReportException{
		Pattern pattern  = Pattern.compile(CHECK_CURRENT_DATE_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.CURRENT_DATE.equals(matcher.group(1)));
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindReference () throws IOException, XDocReportException {
		Pattern pattern =  Pattern.compile(CHECK_REFERENCE_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.REFERENCE.equals(matcher.group(1)));
	}
	
	@Test(expected = Test.None.class) 
	public void givenPdfContentShouldFindAdress()  throws IOException, XDocReportException{
		Pattern pattern = Pattern.compile(PDFServiceConstantsMock.ADDRESS);
		Matcher  matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
	}
		
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindMotif ()  throws IOException, XDocReportException{
		Pattern pattern =  Pattern.compile(CHECK_MOTIF_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.MOTIF.equals(matcher.group(1)));
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindDepotDate () throws IOException, XDocReportException {
		Pattern pattern =  Pattern.compile(CHECK_DATEDEPOT_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.DEPOSITE_DATE.equals(matcher.group(1)));
	}
	
	@Test(expected = Test.None.class) 
	public void givenPdfContentShouldFindComment() throws IOException, XDocReportException {
		Pattern pattern = Pattern.compile(PDFServiceConstantsMock.COMMENT);
		Matcher  matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
	}

	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindRaisonSocial() throws IOException, XDocReportException {
		Pattern pattern = Pattern.compile(PDFServiceConstantsMock.RAISON_SOCIAL);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindStartDate() throws IOException, XDocReportException {
		Pattern pattern = Pattern.compile(CHECK_BEGIN_DATE_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.BEGIN_DATE.equals(matcher.group(1)));
		
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldFindEndDate() throws IOException, XDocReportException {
		Pattern pattern = Pattern.compile(CHECK_END_DATE_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());
		assertTrue(matcher.find());
		assertTrue(PDFServiceConstantsMock.END_DATE.equals(matcher.group(1)));
	}

	@Test(expected = Test.None.class) 
	public void givenPdfcontentCheckTheCommentPosition() throws IOException, XDocReportException {
		
		String[] lines = extractPdfText().split("\n");
		int begin = 0;
		int end = 0;
		
		for(int i = 0; i < lines.length; i++) {
			
			if(lines[i].trim().equals("ma part.")) {
				begin = i;
			}
			
			if(lines[i].trim().equals(PDFServiceConstantsMock.COMMENT)) {
				end  = i;
			}
		}
				
		System.out.println("Begin : " + begin);
		System.out.println("End : " + end);
		assertTrue(end-begin == 2);
	}
	
	@Test(expected = Test.None.class)
	public void givenPdfContentShouldNotHaveBlankLigneForBlankField() throws IOException, XDocReportException {
		
		Pattern pattern = Pattern.compile(CHECK_BLANK_FIELD_ADJASMENT_PATTERN);
		Matcher matcher = pattern.matcher(extractPdfText());

		
		assertTrue(matcher.find());
	}
}
