package mc.gouv.xaf.rio.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ArchivageFichierDeposeDTO {

    private String rang;

    private String nom;

    private String nomTiff;

    private String statut;

    private String date;
    private String referenceDossier;

}
