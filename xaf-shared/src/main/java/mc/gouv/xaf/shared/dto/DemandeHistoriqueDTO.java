package mc.gouv.xaf.shared.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Modélise le contenu d'une ligne d'historique
 *
 * @author qdeme
 */
@Setter
@Getter
@ToString
public class DemandeHistoriqueDTO {

    private Integer pkDemandeHistorique;

    private Integer fkDemandes;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private DemandeStatutDTO fkStatut;

    private String agentId;

    private Integer usagerId;

    private String justificatifTraitement;

    @NotNull
    private JsonNode contenu;

}
