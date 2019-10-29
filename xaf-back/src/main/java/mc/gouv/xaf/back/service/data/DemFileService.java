package mc.gouv.xaf.back.service.data;

import java.net.MalformedURLException;

import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * Service permettant de faire appel aux WS de FILE afin de lier les fichiers aux démarches
 * 
 * @author qdeme
 *
 */
public interface DemFileService {

    /**
     * Appelle le WS FILE PATCH sur chaque fichier afin d'y inscrire la demandeId dans les métadonnées
     * du fichier.
     * @param fichiers
     * @param demande
     * @throws MalformedURLException 
     * @throws Exception 
     */
    public void updateFilesMetadataWithDemandeId(DemandeFileDTO[] fichiers, String demarcheId, Integer demandeId) throws MalformedURLException;
    
}
