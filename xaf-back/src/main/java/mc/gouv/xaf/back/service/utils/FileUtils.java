package mc.gouv.xaf.back.service.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.itg.resid.dto.ResidPieceJustificativeDTO;
import mc.gouv.xaf.shared.itg.resid.enums.ResidPieceJustificativeTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import static mc.gouv.xaf.shared.itg.resid.enums.ResidPieceJustificativeTypeEnum.NON_APPLICABLE;

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

    public static Map<Integer, DemandeFileDTO> getAllFilesAEnvoyerResid(DemandeDTO demandeDTO) {
        Map<Integer, DemandeFileDTO> filesResid = new HashMap<>();
        List<DemandeFileDTO> files = getAllFileDemande(demandeDTO);
        for (int i=0; i<files.size(); i++ ) {
            DemandeFileDTO fileDTO = files.get(i);
            // Check si non applicable côté TS
            if (!NON_APPLICABLE.name().equals(fileDTO.getTypedoc()) && fileDTO.getTypedoc() != null) {
                filesResid.put(i, fileDTO);
            }
        }
        return filesResid;
    }

    public static String formatFilenameResid(String filename, Integer index) {
        return removeSpecialChars(index + "_" + filename);
    }

    public static String removeSpecialChars(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9_]", "_");
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
