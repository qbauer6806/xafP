package mc.gouv.xaf.back.data.projection;

import tools.jackson.databind.JsonNode;
import java.util.Date;

public interface BrouillonPageableProjection {

    Integer getPkBrouillons();

    JsonNode getContenu();

    Date getDateCreation();

    Date getDateDerModif();

    String getBuildId();

    String getRecapType();
}
