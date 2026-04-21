package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.BrouillonBO;
import mc.gouv.xaf.shared.dto.BrouillonFileDTO;

/**
 * Service permettant la manipulation des fichiers joints aux brouillons.
 *
 * @author qdeme
 */
public interface BrouillonsFilesService {

    void saveFiles(BrouillonFileDTO[] brouillonFiles, BrouillonBO brouillonBo);

}
