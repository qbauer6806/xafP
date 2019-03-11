package mc.gouv.af.back.pdf;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import mc.gouv.af.back.AfBackServiceTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=AfBackServiceTestConfiguration.class)
public class PdfGenerationTest {
	
	
	@Autowired
	private PdfGenerationService pdfGenerationService;
	
    @Autowired
    ApplicationContext applicationContext;

    public void printBeans() {
        System.out.println(Arrays.asList(applicationContext.getBeanDefinitionNames()));
    }

	@Test
	public void shouldCreateCorrectPDF() {
	    
	    printBeans();

		File f = pdfGenerationService.generatePdf(GenericDemandeDtoMockGenerator.givenGenericMockDemandeDTO());
		assertNotNull(f);
	    
	}

	
}
