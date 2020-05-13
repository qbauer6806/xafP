package mc.gouv.xaf.backweb.formbean;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Formulaire pour les propriétés de la démarhe
 */
public class PropertiesFormBean {

    private Integer pkProperties;

    @NotEmpty
    @NotNull(message = "Le type doit être précisé")
    @Size(min = 1, max = 256, message = "Le type doit avoir une taille comprise entre 1 et 256")
    private String type;

    @NotEmpty
    @NotNull(message = "La clé doit être précisée")
    @Size(min = 1, max = 256, message = "La clé doit avoir une taille comprise entre 1 et 256")
    private String key;

    @NotEmpty
    @NotNull(message = "La valeur doit être précisée")
    @Size(min = 1, max = 10000, message = "La valeur doit avoir une taille comprise entre 1 et 10000")
    private String value;

    public Integer getPkProperties() {
        return pkProperties;
    }

    public void setPkProperties(Integer pkProperties) {
        this.pkProperties = pkProperties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
