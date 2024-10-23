package mc.gouv.xaf.shared.dto;

import java.util.Date;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

/**
 * Modélise la partie "Question" d'une demande d'informations complémentaires
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemandeComplementsQuestionDTO {

    @NotNull
    private String texte;

    @NotNull
    private String codeMotif;

    @JsonFormat(locale = "fr", shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "GMT+1")
    private Date date;

    @NotNull
    private String agentId;

}
