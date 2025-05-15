package mc.gouv.xaf.back.pdf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import fr.opensagres.xdocreport.core.XDocReportException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mc.gouv.xaf.back.service.pdf.impl.PdfGenerationServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * Les tests unitaires pour le PdfGenerationService
 *
 * @author dsaidiparto.ext
 */
@Disabled
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class PdfGenerationTest {

    private static final String CHECK_TIITLE_FAMILY_PATTERN = "(\\w*) (\\w*) (\\w*) qui aura lieu le";
    private static final String CHECK_IDENTIFIER_PATTERN = "Objet : Votre demande (\\w*)";
    private static final String CHECK_REFERENCE_PATTERN = "Reference : (\\w*)";
    private static final String CHECK_MOTIF_PATTERN = "Motif : (\\w*)";
    private static final String CHECK_DATEDEPOT_PATTERN = "Date de déposition : (\\d{2}/\\d{2}/\\d{4})";
    private static final String CHECK_CURRENT_DATE_PATTERN = "Monaco, le (\\d{2}/\\d{2}/\\d{4})";
    private static final String CHECK_BEGIN_DATE_PATTERN = "lieu le (\\d{2}/\\d{2}/\\d{4})";
    private static final String CHECK_END_DATE_PATTERN = "au (\\d{2}/\\d{2}/\\d{4})";
    private static final String CHECK_BLANK_FIELD_ADJASMENT_PATTERN = "_RAPPELLE_\\n_FIN_RAPPELLE_";

    @Autowired
    private PdfGenerationServiceImpl pdfGenerationService;

    @Autowired
    ApplicationContext applicationContext;

    /**
     * Fournit le texte pour créer le pdf avec les donner fournir en utilisant le template en format docx
     *
     * @return
     * @throws IOException
     * @throws XDocReportException
     */
    private String extractPdfText() throws IOException, XDocReportException {

        byte[] bytes = null;
        StringBuilder content = new StringBuilder();

        //        bytes = pdfGenerationService.generatePdfToStream(GenericDemandeDtoMockGenerator.getGenericMockDemandeDTO());

        PdfReader reader = new PdfReader(bytes);
        PdfTextExtractor extractor = new PdfTextExtractor(reader);

        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            content.append(extractor.getTextFromPage(i));
        }

        return content.toString();
    }

    /**
     * Verifie que le identifiant est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindCorrectUserIdentifier() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_IDENTIFIER_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.IDENTIFIER, matcher.group(1));
    }

    /**
     * Verifie que Ttire, Prénom et Nom sont bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindTittleFirstNameLastName() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_TIITLE_FAMILY_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.TITLE, matcher.group(1));
        assertEquals(PDFServiceConstantsMock.FIRST_NAME, matcher.group(2));
        assertEquals(PDFServiceConstantsMock.LAST_NAME, matcher.group(3));
    }

    /**
     * Verifie que la date courant est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindCurrentDateField() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_CURRENT_DATE_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.CURRENT_DATE, matcher.group(1));
    }

    /**
     * Verifie que le référence est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindReference() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_REFERENCE_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.REFERENCE, matcher.group(1));
    }

    /**
     * Verifie que l'adress est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindAdress() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(PDFServiceConstantsMock.ADDRESS);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
    }

    /**
     * Verifie que le motif est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindMotif() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_MOTIF_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.MOTIF, matcher.group(1));
    }

    /**
     * Verifie que la date de depot est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindDepotDate() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_DATEDEPOT_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.DEPOSITE_DATE, matcher.group(1));
    }

    /**
     * Verifie que le commentaire est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindComment() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(PDFServiceConstantsMock.COMMENT);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
    }

    /**
     * Verifie que le raison sociale est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindRaisonSocial() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(PDFServiceConstantsMock.RAISON_SOCIAL);
        Matcher matcher = pattern.matcher(extractPdfText());
        System.out.println(extractPdfText());
        assertTrue(matcher.find());
    }

    /**
     * Verifie que la date de début est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindStartDate() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_BEGIN_DATE_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.BEGIN_DATE, matcher.group(1));

    }

    /**
     * Verifie que la date de fin est bien placé dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldFindEndDate() throws IOException, XDocReportException {
        Pattern pattern = Pattern.compile(CHECK_END_DATE_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());
        assertTrue(matcher.find());
        assertEquals(PDFServiceConstantsMock.END_DATE, matcher.group(1));
    }

    /**
     * Verifie que generateur de pdf respect la position de commentaire dans le texte
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfcontentCheckTheCommentPosition() throws IOException, XDocReportException {

        String[] lines = extractPdfText().split("\n");
        int begin = 0;
        int end = 0;

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].trim().equals("ma part.")) {
                begin = i;
            }
            if (lines[i].trim().equals(PDFServiceConstantsMock.COMMENT)) {
                end = i;
            }

        }

        System.out.println("Begin : " + begin);
        System.out.println("End : " + end);
        assertEquals(2, end - begin);
    }

    /**
     * Verification de champs conditionelle dans le template , si le parametre est vide il ne faut pas avoire ligne
     * vide
     *
     * @throws IOException
     * @throws XDocReportException
     */
    @Test()
    void givenPdfContentShouldNotHaveBlankLigneForBlankField() throws IOException, XDocReportException {

        Pattern pattern = Pattern.compile(CHECK_BLANK_FIELD_ADJASMENT_PATTERN);
        Matcher matcher = pattern.matcher(extractPdfText());

        assertTrue(matcher.find());
    }

}
