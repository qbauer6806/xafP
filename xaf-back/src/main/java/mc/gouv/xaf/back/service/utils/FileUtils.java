package mc.gouv.xaf.back.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import mc.gouv.xaf.back.data.es.model.DemandeFileEsDTO;
import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.service.pdf.PdfTypeEnum;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.FileCategoryDTO;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
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

    // Categories fichiers pour DemandeFilesCategorizer
    public static final String CAT_INITIALE = "Fichiers de la demande initiale";
    public static final String CAT_COMPLEMENTS = "Fichiers complémentaires de l'usager";
    public static final String CAT_ADMINISTRATION = "Fichiers remis par l'Administration";
    public static final String CAT_INTERNES = "Fichiers internes";

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
        Tika tika = new Tika();
        Reader fulltext = null;
        String contentStr = null;
        try {
            fulltext = tika.parse(stream);
            contentStr = IOUtils.toString(fulltext);
        } finally {
            fulltext.close();
        }
        return contentStr;
    }

    public static List<DemandeFileDTO> getAllFileDemande(DemandeDTO demandeDTO) {
        List<DemandeFileDTO> files = new ArrayList<>();
        // Fichiers de la demande
        if(demandeDTO.getFichiers() != null) {
            files.addAll(Arrays.asList(demandeDTO.getFichiers()));
        }

        // Fichiers des infos compl
        if (demandeDTO.getComplements() != null) {
            for (DemandeComplementsDTO compl : demandeDTO.getComplements()) {
                if (compl.getReponse() != null && compl.getReponse().getFichiers() != null) {
                    files.addAll(DemandesComplementsFilesTransformer.toDemandeFileDTO(Arrays.asList(compl.getReponse().getFichiers())));
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
            nomFichier = nomFichier.substring(0, 150-extension.length()) + extension;
        }
        return nomFichier;
    }

    public static String removeSpecialChars(String filename) {
        String[] filenameExtensionSplit = filename.split("\\.");
        String extension = filenameExtensionSplit[filenameExtensionSplit.length-1];
        // On supprime l'exension du split
        String[] filenameSplit = Arrays.copyOf(filenameExtensionSplit, filenameExtensionSplit.length-1);
        String filenameConcat = String.join("", filenameSplit);
        return filenameConcat.replaceAll("[^a-zA-Z0-9_]", "_") + "." + extension;
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

    public static int getNbFileNonTypes(List<FileCategoryDTO> filesAvecCategorie) {
        int nbSansCategorie = 0;
        for(FileCategoryDTO categoryDTO : filesAvecCategorie) {
            if (FileUtils.CAT_INITIALE.equals(categoryDTO.getName()) || FileUtils.CAT_COMPLEMENTS.equals(categoryDTO.getName())) {
                for (DemandeFileDTO file : categoryDTO.getFiles()) {
                    if (file.getTypedoc() == null) {
                        nbSansCategorie++;
                    }
                }
            }
        }
        return nbSansCategorie;
    }
    /**
     * Méthode permettant de récupérer le type du fichier associé à la demande en se basant sur ses metas
     *
     * @param file fichier dont on doit vérifier le type
     * @return Type du fichier
     */
    public static DemandeFileEsDTO.TYPE getDemandeFileType(DemandeFileDTO file) {
        DemandeFileEsDTO.TYPE fileType;
        if (FileUtils.isFileCreatedByFront(file.getMeta())) {
            fileType = DemandeFileEsDTO.TYPE.PIECE_JOINTE;
        }
        if (FileUtils.isFileCreatedByBack(file.getMeta()) && file.getMeta().contains(PdfTypeEnum.COURRIER.name())) {
            fileType = DemandeFileEsDTO.TYPE.COURRIER;
        } else {
            fileType = DemandeFileEsDTO.TYPE.FICHIER_INTERNE;
        }
        return fileType;
    }

}
