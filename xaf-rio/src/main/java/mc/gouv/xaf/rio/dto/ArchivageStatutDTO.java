package mc.gouv.xaf.rio.dto;

import lombok.Getter;
import lombok.Setter;
import mc.gouv.xaf.rio.enums.ArchivageStatutAvancementEnum;

@Setter
@Getter
public class ArchivageStatutDTO {

    private double progression;

    private int nbFichiersEnErreur;

    private ArchivageStatutAvancementEnum avancement;

}
