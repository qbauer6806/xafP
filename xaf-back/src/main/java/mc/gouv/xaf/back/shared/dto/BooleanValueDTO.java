package mc.gouv.xaf.back.shared.dto;

import javax.validation.constraints.NotNull;

/**
 * Utile dans le cadre de la réponse du WS de vérification de mot de passe
 * 
 * @author qdeme
 *
 */
public class BooleanValueDTO {

    @NotNull
    private boolean booleanValue;

    public boolean isBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }
}
