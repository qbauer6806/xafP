package mc.gouv.xaf.shared.dto;

import java.util.Date;

public interface DemandeRecapProjection {

    Integer getPkDemandes();

    String getIdentifiant();

    Date getDateCreation();

    String getDernierStatut();

}
