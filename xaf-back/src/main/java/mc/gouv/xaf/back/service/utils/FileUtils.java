package mc.gouv.xaf.back.service.utils;

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

    public static final List<DemandeFileDTO> getAllFileDemande(DemandeDTO demandeDTO) {
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
