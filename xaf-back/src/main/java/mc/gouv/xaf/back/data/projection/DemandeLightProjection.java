package mc.gouv.xaf.back.data.projection;

import java.util.Date;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;

public interface DemandeLightProjection {

    Integer getPkDemandes();

    DemandesStatutsBO getDernierStatut();

    String getIdentifiant();

    String getCanal();

    String getLangue();

    Date getDateCreation();
}
