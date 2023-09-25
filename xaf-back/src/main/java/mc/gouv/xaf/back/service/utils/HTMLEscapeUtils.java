package mc.gouv.xaf.back.service.utils;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

/**
 * Classe utilitaire pour échapper des caractères HTML
 * 
 * @author mboutelier.ext
 *
 */
public class HTMLEscapeUtils {

    private HTMLEscapeUtils() {
        throw new IllegalStateException("Utility class");
    }
    
    // Ajoutez ici les caractères à échapper
    private static final String[][] HTML_ESCAPE_CHARS = {
            {";", "&semi;"} // ; - semicolon
        };
    
    public static final CharSequenceTranslator ESCAPE_HTML = 
            new AggregateTranslator(
                new LookupTranslator(EntityArrays.BASIC_ESCAPE()),
                new LookupTranslator(EntityArrays.ISO8859_1_ESCAPE()),
                new LookupTranslator(EntityArrays.HTML40_EXTENDED_ESCAPE()),
                new LookupTranslator(HTML_ESCAPE_CHARS)
            );

    /**
     * échappe les caractères de la chaîne donnée
     * @param txt
     * @return
     */
    public static String escape(String txt) {
        return ESCAPE_HTML.translate(txt);
    }

}
