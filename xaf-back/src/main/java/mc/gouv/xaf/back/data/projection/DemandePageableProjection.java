package mc.gouv.xaf.back.data.projection;

import java.util.Date;

public interface DemandePageableProjection {

    Integer getPkDemandes();

    Date getDateCreation();

    String getIdentifiant();

    String getUsagerPrenom();

    String getUsagerNom();

    Integer getPkStatut();

    String getStatutLibelle();

    String getStatutName();

    Date getStatutDate();
}
