package mc.gouv.xaf.shared.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * Représente une période d'ouverture d'une démarche
 *
 * @author qdeme
 */
@Setter
@Getter
public class PeriodeOuvertureDTO {

    private Integer pkPeriodesOuverture;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date dateDebut;

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    private Date dateFin;

    private String demarcheId = "STAGE";

}
