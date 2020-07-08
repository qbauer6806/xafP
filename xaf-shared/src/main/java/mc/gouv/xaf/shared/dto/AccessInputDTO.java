package mc.gouv.xaf.shared.dto;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Input de WS pour les demandes
 * 
 * @author qdeme
 *
 */
public class AccessInputDTO {

    @NotNull
    private JsonNode contenu;

    public JsonNode getContenu() {
        return contenu;
    }

    public void setContenu(JsonNode contenu) {
        this.contenu = contenu;
    }
    
}
