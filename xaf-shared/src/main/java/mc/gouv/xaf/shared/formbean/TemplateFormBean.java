package mc.gouv.xaf.shared.formbean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire de la page de gestion des templates
 *
 * @author mpavone
 */
@Setter
@Getter
public class TemplateFormBean {

    @NotEmpty
    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code;

    @NotNull(message = "L'objet du mail doit être précisé")
    @Size(min = 3, max = 256, message = "L'objet du mail doit avoir une taille comprise entre 3 et 256")
    private String objet;

    @NotNull(message = "Le contenu du mail doit être précisé")
    @Size(max = 5000, message = "Le contenu doit avoir une taille comprise entre 0 et 5000")
    private String corps;

    @NotNull(message = "La langue doit être précisée")
    @Size(min = 2, max = 2, message = "Le format de la langue n'est pas correct")
    private String langue;

    private String date;

    private Integer pkDemandeTest;

}
