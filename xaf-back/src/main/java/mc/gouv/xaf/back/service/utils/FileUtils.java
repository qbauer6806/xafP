package mc.gouv.xaf.back.service.utils;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

/**
 * 
 * Classe utilitaire pour traiter les fichiers
 * 
 * @author asouabni.ext
 *
 */
@Component
public class FileUtils {
	
	public static final String META_BACK = "BACK_";
	
	public static final String META_FRONT = "FRONT_";
	
	public static final String META_BACK_FRONT = "BACK_FRONT_";

    private FileUtils() {
    }

    /**
     * Méthode permettant de lire le contenu d'un fichier
     * 
     * @param stream
     *            InputStream à lire
     * @return Le fichier sous forme d'une chaine de caractéres
     * @throws IOException
     * @throws SAXException
     * @throws TikaException
     */
    public static final String parseToPlainText(InputStream stream) throws IOException, SAXException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        parser.parse(stream, handler, metadata);
        return handler.toString();
    }

    /**
     * Méthode pemrmettant de détecter la langue à partir d'une chaine de caractéres
     * 
     * @param text
     *            Texte dont on veut detecter la langue
     * @return Langue detectée
     * @throws IOException
     */
    public static String detectLanguage(String text) throws IOException {
        // On prend les 20k premiers caractères pour éviter des fichiers trop grands
        String textToAnalyze = text.substring(0, 20000);
        LanguageDetector detector = new OptimaizeLangDetector().loadModels();
        LanguageResult result = detector.detect(textToAnalyze);
        return result.getLanguage();
    }
	
    // Norme sur les métadonnées des fichiers
    public static boolean isFileCreatedByFront(String meta) {
    	return (StringUtils.isBlank(meta) || meta.startsWith(META_FRONT));
    }
    
    public static boolean isFileCreatedByBack(String meta) {
    	return (!StringUtils.isBlank(meta) && !meta.startsWith(META_FRONT));
    }
    
    public static boolean isFileCreatedByBackVisibleByFront(String meta) {
    	return (!StringUtils.isBlank(meta) && meta.startsWith(META_BACK_FRONT));
    }
    // FIN Norme sur les métadonnées des fichiers

}
