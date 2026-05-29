package mc.gouv.xaf.back.data.projection;

import java.util.Date;

public interface DemandePageableProjection {

    Integer getPkDemandes();

    Date getDateCreation();

    String getIdentifiant();

    Integer getPkStatut();

    String getStatutLibelle();

    String getStatutName();

    Date getStatutDate();
}
