package mc.gouv.xaf.back.data.projection;

import java.util.Date;

public interface BrouillonPageableProjection {

    Integer getPkBrouillons();

    Date getDateCreation();

    Date getDateDerModif();

    String getBuildId();

    String getRecapType();
}
