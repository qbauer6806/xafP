package mc.gouv.xaf.shared.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ExportMotifDTO {

    private String code;

    private String libelle;

    private String statut;

    private String statutCourant;

    private String langue;

    private Date dateArchive;

    private String commentairePrerempli;

    private String texteAEnvoyer;
}
