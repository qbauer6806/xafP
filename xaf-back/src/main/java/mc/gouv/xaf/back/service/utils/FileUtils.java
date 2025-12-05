package mc.gouv.xaf.back.service.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

/**
 * Classe utilitaire pour traiter les fichiers
 *
 * @author asouabni.ext
 */
public class FileUtils {

    public static final String META_BACK = "BACK_";
    public static final String META_FRONT = "FRONT_";
    public static final String META_BACK_FRONT = "BACK_FRONT_";
    public static final String META_BACK_FRONT_SYSTEME_TIERS = "BACK_FRONT_SYSTEME_TIERS";
    public static final String META_FRONT_IDX = "FRONT_IDX_";
    public static final String META_RECAP = META_BACK + "RECAP";
    public static final String META_COMPLEMENT = META_BACK + "COMPLEMENT";

    // Categories fichiers pour DemandeFilesCategorizer
    public static final String CAT_INITIALE = "Fichiers de la demande initiale";
    public static final String CAT_COMPLEMENTS = "Fichiers complémentaires de l'usager";
    public static final String CAT_ADMINISTRATION = "Fichiers remis par l'Administration";
    public static final String CAT_INTERNES = "Fichiers internes";

    public static final String DEFAULT_CONTAINER = "ROOT";

    public static final String MC_METADATA_PREFIX = "X-MC-";

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Méthode permettant de lire le contenu d'un fichier
     *
     * @param stream
     *         InputStream à lire
     * @return Le fichier sous forme d'une chaine de caractéres
     */
    public static String parseToPlainText(InputStream stream) throws IOException {
        Tika tika = new Tika();
        Reader fulltext = null;
        String contentStr;
        try {
            fulltext = tika.parse(stream);
            contentStr = IOUtils.toString(fulltext);
        } finally {
            if (fulltext != null) {
                fulltext.close();
            }
        }
        return contentStr;
    }

    public static List<DemandeFileDTO> getAllFileDemande(DemandeDTO demandeDTO) {
        List<DemandeFileDTO> files = new ArrayList<>();
        // Fichiers de la demande
        if (demandeDTO.getFichiers() != null) {
            files.addAll(Arrays.asList(demandeDTO.getFichiers()));
        }

        // Fichiers des infos compl
        if (demandeDTO.getComplements() != null) {
            for (DemandeComplementsDTO compl : demandeDTO.getComplements()) {
                if (compl.getReponse() != null && compl.getReponse().getFichiers() != null) {
                    files.addAll(DemandesComplementsFilesTransformer.toDemandeFileDTO(
                            Arrays.asList(compl.getReponse().getFichiers())));
                }
            }
        }
        return files;
    }

    public static String formatFilenameResid(String filename, Integer index) {
        String nomFichier = removeSpecialChars(index + "_" + filename);
        // Tronquer si plus de 150 chars dans la requête. Attention à l'extension !
        if (nomFichier.length() > 150) {
            String extension = nomFichier.substring(nomFichier.lastIndexOf('.'));
            nomFichier = nomFichier.substring(0, 150 - extension.length()) + extension;
        }
        return nomFichier;
    }

    public static String removeSpecialChars(String filename) {
        String[] filenameExtensionSplit = filename.split("\\.");
        String extension = filenameExtensionSplit[filenameExtensionSplit.length - 1];
        // On supprime l'exension du split
        String[] filenameSplit = Arrays.copyOf(filenameExtensionSplit, filenameExtensionSplit.length - 1);
        String filenameConcat = String.join("", filenameSplit);
        return filenameConcat.replaceAll("\\W", "_") + "." + extension;
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

    public static boolean isFileComplement(String meta) {
        return (!StringUtils.isBlank(meta) && meta.contains(META_COMPLEMENT));
    }
    // FIN Norme sur les métadonnées des fichiers

    public static int getNbFileNonTypes(List<FileCategoryDTO> filesAvecCategorie) {
        int nbSansCategorie = 0;
        for (FileCategoryDTO categoryDTO : filesAvecCategorie) {
            if (categoryDTO.isTypedoc()) {
                for (DemandeFileDTO file : categoryDTO.getFiles()) {
                    if (StringUtils.isEmpty(file.getTypedoc())) {
                        nbSansCategorie++;
                    }
                }
            }
        }
        return nbSansCategorie;
    }

    /**
     * Génère des métas à partir des données du fichier
     *
     * @param file
     *         le fichier en question
     * @return une {@link String} au format 'meta' en clef_valeur séparé par un point-virgule
     */
    public static String generateMetaData(File file) throws IOException {
        Tika tika = new Tika();
        long fileSizebytes = file.length();
        String mimetype = tika.detect(file);

        return formatMetaData(fileSizebytes, mimetype);
    }

    /**
     * Génère des métas à partir des données du fichier
     *
     * @param file
     *         le fichier en question
     * @return une {@link String} au format 'meta' en clef_valeur séparé par un point-virgule
     */
    public static String generateMetaData(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        long fileSizebytes = file.getSize();
        String mimetype = StringUtils.isNotEmpty(file.getContentType())
                ? file.getContentType()
                : tika.detect(file.getInputStream());

        return formatMetaData(fileSizebytes, mimetype);
    }

    private static String formatMetaData(long fileSizebytes, String mimetype) {
        return "SIZE_" + fileSizebytes + ";TYPE_" + mimetype;
    }

    /**
     * Sanitize le nom de fichier pour éviter les path traversal attacks
     */
    public static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException("Le nom de fichier ne peut pas être null");
        }

        // Remplace tous les caractères potentiellement dangereux
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

}
