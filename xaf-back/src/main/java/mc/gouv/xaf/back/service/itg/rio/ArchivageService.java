package mc.gouv.xaf.back.service.itg.rio;

import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface ArchivageService {

    Map<Integer, Double> archivageProgress = new ConcurrentHashMap<>();

    /**
     * Archive les documents en attribut
     *
     * @param refPermis Référence permis
     * @param files     Fichiers à archiver
     * @param demandeId PK demande
     * @return Liste des fichiers archivés
     */
    List<DemandeFileDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files, int demandeId);
}
