package mc.gouv.xaf.shared.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DemandeJobDTO {

    private String jobName;
    private Date dateCreation;
    private Date dateDernModif;
    private String msg;
    private String statut;
    private String statutCode;

}
