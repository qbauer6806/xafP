package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 *
 * @author qdeme
 */
public interface DemandesFilesService {

    void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo);

    void saveFile(DemandeFileDTO demandeFile, Integer pkDemande);

    void saveFile(DemandeFileDTO demandeFile, Integer pkDemande, boolean checkActive);

    boolean updateTypedocs(Map<String, String> changes, Map<String, Boolean> checkboxes);

    List<DemandeFileDTO> getFileByDemandeIdAndTypedoc(Integer pkDemande, String typedoc);

    List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta);

    /**
     * Duplication des pièces jointes d'une demande vers une nouvelle demande.
     *
     * @param demandeBO
     *         L'objet BO de la demande à cloner
     * @param newDemandeBO
     *         le nouvel objet BO
     */
    void clonerDesPiecesJointes(DemandeBO demandeBO, DemandeBO newDemandeBO);

    /**
     * Permet de mettre à jour les fichiers d'une demande.
     *
     * @param demandeBo
     *         L'objet BO de la demande à mettre à jour
     * @param fichiers
     *         La liste de fichiers à ajouter à la demande.
     */
    void updateFichiers(DemandeBO demandeBo, DemandeFileDTO[] fichiers);

    /**
     * Suppression des fichiers liés à la demande au moment de la supression de cette dernière.
     *
     * @param demandeDTO
     *         La demande à supprimer
     */
    void suppressionDesFichiers(DemandeDTO demandeDTO);

    /**
     * Récupère un objet DemandeFileDTO à partir de l’identifiant fourni.
     *
     * @param pkDemandesFiles
     *         L’identifiant du fichier à récupérer.
     * @return Un Optional contenant le DemandeFileDTO s’il est trouvé, ou un Optional vide si aucun fichier n’est
     *         associé à l’identifiant fourni.
     */
    Optional<DemandeFileDTO> getFileByDemandeFileId(Integer pkDemandesFiles);
}
