package mc.gouv.xaf.back.data.projection;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.DemandesAgentsBO;
import mc.gouv.xaf.back.data.entity.DemandesStatutsBO;
import mc.gouv.xaf.back.data.entity.DemandesUsagersBO;

import java.util.Date;

public interface DemandeExportProjection {

    Integer getPkDemandes();

    Date getDateCreation();

    Date getDateDerModif();

    JsonNode getContenu();

    JsonNode getContenuTrad();

    String getLangue();

    String getCanal();

    String getObservations();

    DemandesAgentsBO getAgent();

    DemandesUsagersBO getUsager();

    DemandeConfigBO getConfig();

    DemandesStatutsBO getDernierStatut();

    String getIdentifiant();
}
