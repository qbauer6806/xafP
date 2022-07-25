package mc.gouv.xaf.back.paiement.service;

import mc.gouv.xaf.back.paiement.dto.itg.rio.FileDocumentDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;

public interface ArchivageService {


    /**
     * Archive les documents en attribut
     *
     * @param refPermis Réference permis
     * @param files     Fichiers à archiver
     * @return Liste des fichiers archivés
     */
    List<FileDocumentDTO> archivageDocuments(String refPermis, List<DemandeFileDTO> files);
}
