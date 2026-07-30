package mc.gouv.xaf.back.data.projection;

import tools.jackson.databind.JsonNode;
import java.util.Date;

public interface DemandePageableProjection {

    Integer getPkDemandes();

    Date getDateCreation();

    JsonNode getContenu();

    String getIdentifiant();

    String getUsagerPrenom();

    String getUsagerNom();

    Integer getPkStatut();

    String getStatutLibelle();

    String getStatutName();

    Date getStatutDate();
}
