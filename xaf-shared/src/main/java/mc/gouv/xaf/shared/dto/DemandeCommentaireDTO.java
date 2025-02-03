package mc.gouv.xaf.shared.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class DemandeCommentaireDTO {

    private Integer pkDemandeCommentaire;

    private Integer fkDemandes;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private String agentId;

    private String commentaire;

}
