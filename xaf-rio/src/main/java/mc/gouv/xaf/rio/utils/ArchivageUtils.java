package mc.gouv.xaf.rio.utils;

import mc.gouv.xaf.back.data.transformer.DemandesComplementsFilesTransformer;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Classe utilitaire pour l'archivage des fichiers d'une demande
 */
public class ArchivageUtils {

    public static final String ARCHIVAGE_RIO_COMPLETED = "ARCHIVAGE_RIO_COMPLETED";
    public static final String NOMBRE_FICHIERS_ERREUR_ARCHIVAGE = "NOMBRE_FICHIERS_ERREUR_ARCHIVAGE";
    public static final String CODE_TYPE_PERMIS = "PERMIS";
    public static final String CODE_TYPE_IMMAT = "IMMAT";

    private ArchivageUtils() {
        //DO NOTHING
    }

    /**
     * Permets de trouver une tâche à partir d'une référence
     *
     * @param ref
     *         référence de la tâche à chercher
     * @return
     */
    public static Predicate<TacheDTO> filtrerTache(String ref) {
        return tacheDTO -> {
            if (CODE_TYPE_PERMIS.equals(tacheDTO.getCodeType())) {
                return StringUtils.equals(ref, tacheDTO.getContenu().at("/numPermis").asText());
            } else if (CODE_TYPE_IMMAT.equals(tacheDTO.getCodeType())) {
                return StringUtils.equals(ref, tacheDTO.getContenu().at("/numRegistre").asText());
            }
            return false;
        };
    }

    /**
     * Récupération des fichiers de la demande
     */
    public static List<DemandeFileDTO> getAllFichiers(DemandeDTO demandeDto, String ordreFichiers) {
        if (demandeDto == null) {
            return new ArrayList<>();
        }
        List<DemandeFileDTO> fichiers = demandeDto.getFichiers() == null
                ? new ArrayList<>()
                : new ArrayList<>(Arrays.asList(demandeDto.getFichiers()));
        // Récupération des fichiers complémentaires
        if (demandeDto.getComplements() != null) {
            for (DemandeComplementsDTO complements : demandeDto.getComplements()) {
                if (complements.getReponse() != null) {
                    List<DemandeComplementsFileDTO> demandeFileDTOList = Arrays.asList(
                            complements.getReponse().getFichiers());
                    fichiers.addAll(DemandesComplementsFilesTransformer.toDemandeFileDTO(demandeFileDTOList));
                }
            }
        }
        // refs #43237 - [BO] Qualification des documents : On remove les fichiers qui ne doivent pas partir à l'archivage
        fichiers.removeIf(currentFichier -> null != currentFichier.getTypedoc() && currentFichier.getTypedoc()
                .equals("NON_APPLICABLE"));

        // Gestion de l'ordre d'envoi
        // Si une variable d'ordre est définie, trier les fichiers
        if (StringUtils.isNotBlank(ordreFichiers)) {
            fichiers = trierFichiers(ordreFichiers, fichiers);
        }
        renameFichiers(fichiers);
        return fichiers;
    }

    private static void renameFichiers(List<DemandeFileDTO> fichiers) {
        // Pour chaque fichier on veut le renommer pour qu'il prenne le nom de son type avant archivage
        for (DemandeFileDTO demandeFileDTO : fichiers) {
            String extension = demandeFileDTO.getName().substring(demandeFileDTO.getName().lastIndexOf("."))
                    .toLowerCase();
            demandeFileDTO.setName(demandeFileDTO.getTypedoc() != null
                    ? demandeFileDTO.getTypedoc() + extension
                    : demandeFileDTO.getMeta() + extension);
        }
    }

    /**
     * Permets de trier la liste de fichier à partir de l'ordre fichier passé en paramètre
     *
     * @param ordreFichiers
     * @param fichiers
     * @return
     */
    public static List<DemandeFileDTO> trierFichiers(String ordreFichiers, List<DemandeFileDTO> fichiers) {
        if (StringUtils.isBlank(ordreFichiers)) {
            return fichiers;
        }
        if (fichiers == null) {
            return new ArrayList<>();
        }
        List<DemandeFileDTO> fichiersTries = new ArrayList<>();
        for (String typeDoc : ordreFichiers.split(",")) {
            for (DemandeFileDTO file : fichiers) {
                if (typeDoc.equals(file.getTypedoc())) {
                    fichiersTries.add(file);
                }
            }
        }
        return fichiersTries;
    }

    /**
     * Permets de construire une map de référence et code type référence à partir de la liste des tâches
     *
     * @param taches
     * @param predicat
     * @return
     */
    public static Map<String, String> getReferencesTaches(List<TacheDTO> taches, Predicate<TacheDTO> predicat) {
        if (CollectionUtils.isEmpty(taches) || predicat == null) {
            return new HashMap<>();
        }
        return taches.stream().filter(predicat).collect(Collectors.toMap(getKey(), TacheDTO::getCodeType));
    }

    private static Function<TacheDTO, String> getKey() {
        return tacheDTO -> {
            if (CODE_TYPE_PERMIS.equals(tacheDTO.getCodeType())) {
                return tacheDTO.getContenu().at("/numPermis").asText();
            } else {
                // Sinon on va cherche le numéro de registre
                return tacheDTO.getContenu().at("/numRegistre").asText();
            }
        };
    }
}
