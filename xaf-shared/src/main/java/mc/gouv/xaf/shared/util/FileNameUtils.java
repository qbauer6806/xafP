package mc.gouv.xaf.shared.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Classe utilitaire pour normaliser le nom d'un fichier uploadé
 */
public class FileNameUtils {

    private static final String FILE_NAME_REGEX = "[^a-zA-Z0-9.\\-_]";

    private FileNameUtils() {
        //DO NOTHING
    }

    /**
     * Permet de supprimer les diacritiques (accents, cédilles, etc.) dans le nom du fichier, ainsi que les caractères
     * spéciaux, à l'exception des points, tirets et underscores, sont remplacés par des underscores.
     *
     * @param filename le nom du fichier à renommer
     * @return le nom du fichier renommé
     */
    public static String getSafeFileName(String filename) {
        if (StringUtils.isBlank(filename)) {
            return filename;
        }
        return StringUtils.stripAccents(filename).replaceAll(FILE_NAME_REGEX, "_");
    }
}
