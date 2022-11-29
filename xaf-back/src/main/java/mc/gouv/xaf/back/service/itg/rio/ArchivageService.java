package mc.gouv.xaf.back.service.itg.rio;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.export.archivage.ArchivageStatutDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ArchivageService {

    // Il est impossible de sauvegarder / accéder à une même ressource en parrallèle avec Hibernate
    // Hibernate utilise un pessimist locking qui bloque l'accès concurrent à la même ressource, nous sommes donc
    // obligés de passer par une variable dans le code et sauvegarder uniquement le résultat final en BDD
    Map<Integer, ArchivageStatutDTO> archivageProgress = new ConcurrentHashMap<>();

    /**
     * Archive les documents en attribut
     *
     * @param refPermis     Référence permis
     * @param files         Fichiers à archiver
     * @param demandeDTO    demande
     * @return Liste des fichiers archivés
     */
    List<DemandeFileDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files, DemandeDTO demandeDTO);
}
