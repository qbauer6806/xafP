package mc.gouv.xaf.shared.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Permet la saisie d'un statut
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class StatutInputDTO {

    @NotNull
    private String statut;

    private String codeMotif;

    private String commentaire;

    private String texteAEnvoyer;

    private String agentId;

    private Integer usagerId;

}
