package mc.gouv.xaf.shared.formbean;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Formulaire de la page de gestion des templates
 * 
 * @author mpavone
 * 
 */
public class TemplateFormBean {

    @NotEmpty
    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code;

    @NotEmpty
    @NotNull(message = "L'objet du mail doit être précisé")
    @Size(min = 3, max = 256, message = "L'objet du mail doit avoir une taille comprise entre 3 et 256")
    private String objet;

    @NotNull(message = "Le contenu du mail doit être précisé")
    @Size(min = 0, max = 1500, message = "Le contenu doit avoir une taille comprise entre 0 et 5000")
    private String corps;

    @NotNull(message = "La langue doit être précisée")
    @Size(min = 2, max = 2, message = "Le format de la langue n'est pas correct")
    private String langue;

    @NotNull(message = "Une date de modification doit être renseignée")
    private String date;

    @Size(max = 128, message = "Le type d'audience d'email doit avoir une taille maximale de 128 charactères")
    private String audience;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public String getCorps() {
        return corps;
    }

    public void setCorps(String corps) {
        this.corps = corps;
    }

    public String getLangue() {
        return langue;
    }

    public void setLangue(String langue) {
        this.langue = langue;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }
}
