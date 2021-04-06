package mc.gouv.xaf.back.dsp.utils;

import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.dsp.enums.ResidPieceJustificativeTypeEnum;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 
 * Classe utilitaire pour traiter les fichiers
 * 
 * @author asouabni.ext
 *
 */
@Component
public class FileUtilsResid extends FileUtils {

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
            if (!ResidPieceJustificativeTypeEnum.NON_APPLICABLE.name().equals(fileDTO.getTypedoc()) && fileDTO.getTypedoc() != null) {
                filesResid.put(i, fileDTO);
            }
        }
        return filesResid;
    }
    public static String removeSpecialChars(String filename) {
        String[] filenameExtensionSplit = filename.split("\\.");
        String extension = filenameExtensionSplit[filenameExtensionSplit.length-1];
        // On supprime l'exension du split
        String[] filenameSplit = Arrays.copyOf(filenameExtensionSplit, filenameExtensionSplit.length-1);
        String filenameConcat = String.join("", filenameSplit);
        return filenameConcat.replaceAll("[^a-zA-Z0-9_]", "_") + "." + extension;
    }

}
