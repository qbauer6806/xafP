package mc.gouv.xaf.back.service;

import mc.gouv.xaf.shared.dto.UploadFileDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

public interface UploadPieceJustificativeService {

    /**
     * Enregistre une liste de pièces justificatives uploadés depuis le BO
     * @param pkDemande l'identifiant de la demande
     * @param files les fichiers à enregistrer
     * @param metadonnees les informations des pièces
     * @param response
     * @return le message du résultat du traitement des fichiers
     */
    ResponseEntity<String> enregistrerPieceJustificative(Integer pkDemande, MultipartFile[] files,
            List<UploadFileDTO> metadonnees, HttpServletResponse response);

    /**
     * supprimer un fichier depuis le BO
     * @param idDemandeFile l'identifiant du fichier à supprimer
     * @return le message du résultat du traitement des fichiers
     */
    ResponseEntity<String> supprimerPieceJustificative(Integer idDemandeFile);

    /**
     * Modifie la visibilité d'un fichier associé à une demande.
     *
     * @param idDemandeFile
     *         l'identifiant du fichier dont la visibilité doit être modifiée
     * @param visibleUsager
     *         la nouvelle visibilité du fichier (true pour visible, false pour invisible) par l'usager
     * @return un objet ResponseEntity contenant un message indiquant le résultat de l'opération
     */
    ResponseEntity<String> changerVisibiliteFichier(Integer idDemandeFile, boolean visibleUsager);
}
