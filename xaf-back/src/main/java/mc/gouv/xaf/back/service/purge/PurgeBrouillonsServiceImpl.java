package mc.gouv.xaf.back.service.purge;

import java.util.Date;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.BrouillonsFilesRepository;
import mc.gouv.xaf.back.data.dao.BrouillonsRepository;
import mc.gouv.xaf.back.data.dao.PurgeFilesRepository;
import mc.gouv.xaf.back.service.data.impl.DemandesConfigHelperService;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PurgeBrouillonsServiceImpl implements PurgeBrouillonsService {

    private final BrouillonsRepository brouillonsRepository;
    private final BrouillonsFilesRepository brouillonsFilesRepository;
    private final PurgeFilesRepository purgeFilesRepository;
    private final PurgeDemandesService purgeDemandesService;
    private final DemandesConfigHelperService demandesConfigHelperService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeBrouillonsServiceImpl.class);

    /*
     * on sauvegarde les urls des fichiers des brouillons directement de la table DEM_BROUILLONS_FILES pour les
     * supprimer de file par la suite Les enregistrements de cette table ne sont pas supprimés en cascade lors de la
     * suppression des brouillons car on delete par query, donc il faut supprimer les fichiers explicitement avant la
     * suppression du brouillon
     *
     */

    @Override
    public String purgerBrouillons() {

        Date debut = new Date();
        String currentBuildId = demandesConfigHelperService.getLastBuildId();
        purgeFilesRepository.insertFilesBrouillonsToPurgeWithBuildIdOtherThan(currentBuildId);
        Long totalBrouillons = brouillonsRepository.getCountBrouillonsWithBuildIdOtherThan(currentBuildId);
        brouillonsFilesRepository.deleteBrouillonsFilesWithBuildIdOtherThan(currentBuildId);
        brouillonsRepository.deleteBrouillonsWithBuildIdOtherThan(currentBuildId);

        /*** PURGE DES FICHIERS ***/
        Triple<Integer, Integer, Integer> result = purgeDemandesService.executerPurgeFichiers();

        Date finFichier = new Date();
        StringBuilder sb = new StringBuilder();
        LOGGER.info("Fin purge des Brouillons, {} Brouillons(s) supprimée(s)...", totalBrouillons);
        LOGGER.info("Fin purge des Brouillons, {} fichier(s) supprimé(s)...", result.getLeft());
        LOGGER.info("Fin purge des Brouillons, {} fichier(s) exclus car référencés...", result.getMiddle());
        LOGGER.info("Fin purge des Brouillons, {} appels vers file effectué(s)...", result.getRight());
        LOGGER.info("Fin purge des Brouillons et fichiers en {} secondes",
                (finFichier.getTime() - debut.getTime()) / 1000);

        sb.append(totalBrouillons).append(" brouillons(s) supprimé(s)<br>");
        sb.append(result.getLeft()).append(" fichier(s) supprimé(s)<br>");

        LOGGER.info("fin");
        return sb.toString();
    }

}
