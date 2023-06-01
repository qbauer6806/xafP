package mc.gouv.xaf.rio.service;

import mc.gouv.xaf.rio.dto.ArchivageStatutDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ArchivageService {

    // Il est impossible de sauvegarder / accéder à une même ressource en parrallèle avec Hibernate
    // Hibernate utilise un pessimist locking qui bloque l'accès concurrent à la même ressource, nous sommes donc
    // obligés de passer par une variable dans le code et sauvegarder uniquement le résultat final en BDD
    Map<Integer, ArchivageStatutDTO> archivageProgress = new ConcurrentHashMap<>();

    /**
     * Archive les documents en attribut dans un registre donné
     *
     * @param refPermis     Référence permis
     * @param files         Fichiers à archiver
     * @param demandeDTO    demande
     * @return Liste des fichiers archivés
     */
    List<DemandeFileDTO> archivagePermis(String refPermis, List<DemandeFileDTO> files, DemandeDTO demandeDTO);
    
    /**
     * Archive les documents en attribut dans un registre donné
     * @param refRegistre	Référence permis
     * @param files			Fichiers à archiver
     * @param demandeDTO	demande
     * @return
     */
    List<DemandeFileDTO> archivageRegistre(String refRegistre, List<DemandeFileDTO> files, DemandeDTO demandeDTO);
}
