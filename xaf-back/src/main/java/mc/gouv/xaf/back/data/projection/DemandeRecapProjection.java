package mc.gouv.xaf.back.data.projection;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import java.util.Date;

public interface DemandeRecapProjection {

    Integer getPkDemandes();

    String getIdentifiant();

    Date getDateCreation();

    DemandesStatutsBO getDernierStatut();

}
