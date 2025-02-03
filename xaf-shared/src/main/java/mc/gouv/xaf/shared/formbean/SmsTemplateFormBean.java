package mc.gouv.xaf.shared.formbean;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire de la page de gestion des templates SMS
 *
 * @author qdeme
 */
@Setter
@Getter
public class SmsTemplateFormBean {

    @NotEmpty
    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code;

    @Size(min = 0, max = 11, message = "Le sender doit avoir une taille comprise entre 0 et 11")
    private String sender;

    @NotNull(message = "Le contenu du SMS doit être précisé")
    @Size(max = 450, message = "Le contenu doit avoir une taille comprise entre 1 et 450")
    private String corps;

    @NotNull(message = "La langue doit être précisée")
    @Size(min = 2, max = 2, message = "Le format de la langue n'est pas correct")
    private String langue;

    @NotNull(message = "Une date de modification doit être renseignée")
    private String date;

}
