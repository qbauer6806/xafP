package mc.gouv.af.back.pdf;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFServiceConstantsMock {

    private static DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static final String CURRENT_DATE = DATE_FORMAT.format(new Date());
    public static final String ADDRESS = "13 Avenue JUnit";
    public static final String CODEPOSTAL = "123456";
    public static final String CITY = "TestVille";
    public static final String IDENTIFIER = "TESTDEM-20190319-184S";
    public static final String DATE_DEPOT = "01/03/2019";
    public static final String TITLE = "Mr";
    public static final String FIRST_NAME = "Bob";
    public static final String LAST_NAME = "TestMan";
    public static final String MOTIF = "TestMotif";
    public static final String REFERENCE = "TestREF0000";
    public static final String COMMENT = "Ceci est un commentaire";
    public static final String RAISON_SOCIALE = "Société Test";
    public static final String BEGIN_DATE = "18/04/2019";
    public static final String END_DATE = "27/12/2019";
}
