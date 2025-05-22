package mc.gouv.xaf.back.data.projection;

import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;

public interface DemandeLightProjection {

    Integer getPkDemandes();

    DemandesStatutsBO getDernierStatut();

    String getIdentifiant();
}
