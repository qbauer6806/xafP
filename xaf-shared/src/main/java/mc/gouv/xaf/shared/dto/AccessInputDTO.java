package mc.gouv.xaf.shared.dto;

import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Input de WS pour les demandes
 * 
 * @author qdeme
 *
 */
@Setter
@Getter
public class AccessInputDTO {

    @NotNull
    private JsonNode contenu;

}
