package mc.gouv.xaf.shared.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise la partie "Réponse" d'une demande d'informations complémentaires
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class DemandeComplementsReponseDTO {

    @NotNull
    private String texte;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    private Integer usagerId;

    private String agentId;

    @JsonInclude(Include.NON_NULL)
    private DemandeComplementsFileDTO[] fichiers;

}
