package mc.gouv.xaf.back.dsp.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.back.dsp.enums.common.ResidPieceJustificativeTypeEnum;
import mc.gouv.xaf.back.service.utils.FileUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

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
            if (!isTransmiseAResid(fileDTO)) {
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
    
	public static boolean isTransmiseAResid(DemandeFileDTO fileDTO) {
//		return (fileDTO.getTypedoc() != null
//				&& (ResidPieceJustificativeTypeEnum.NON_APPLICABLE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.ACTE_MARIAGE_OU_FAMILLE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.ACTE_NAISSANCE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.ATTESTATION_COMPTABLE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.AUTORISATION_EMBAUCHAGE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.AUTORISATION_MINISTERIELLE.name()
//								.equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.AVIS_IMPOTS.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.CARTE_PROFESSIONNELLE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.CERTIF_SCOLARITE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.CONTRAT_TRAVAIL.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.DOCUMENT_BANCAIRE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.FACTURE_SMEG.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.JUSTIF_PERTE_NATIONALITE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.ALLOCATION_POLE_EMPLOI.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.NOUVEAU_CONTRAT_ELECTRICITE.name()
//								.equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.PERMIS_TRAVAIL.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.STATUTS_SCI.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.PENSION_RETRAITE.name().equals(fileDTO.getTypedoc())
//						|| ResidPieceJustificativeTypeEnum.AUTRE.name().equals(fileDTO.getTypedoc())));
		return (fileDTO.getTypedoc() != null
				&& (ResidPieceJustificativeTypeEnum.NON_APPLICABLE.name().equals(fileDTO.getTypedoc())
						|| ResidPieceJustificativeTypeEnum.MANDATAIRE.name().equals(fileDTO.getTypedoc())
						|| ResidPieceJustificativeTypeEnum.AUTRE.name().equals(fileDTO.getTypedoc())));

	}

}
