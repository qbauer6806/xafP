package mc.gouv.xaf.shared.formbean;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
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
public class TemplateCreateFormBean {

    @NotEmpty(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Le code doit contenir uniquement des majuscules et des underscores"
    )
    private String code;

    @NotEmpty(message = "L'objet du mail doit être précisé")
    @Size(max = 256, message = "L'objet du mail Français doit avoir une taille comprise entre 3 et 256")
    private String objetFr;

    @NotEmpty(message = "Le contenu du mail doit être précisé")
    @Size(max = 5000, message = "Le contenu Français doit avoir une taille comprise entre 0 et 5000")
    private String corpsFr;

    @Size(max = 256, message = "L'objet du mail Anglais doit avoir une taille comprise entre 3 et 256")
    private String objetEn;

    @Size(max = 5000, message = "Le contenu Anglais doit avoir une taille comprise entre 0 et 5000")
    private String corpsEn;

    private Integer pkDemandeTest;

    @AssertTrue(message = "Le contenu Anglais doit être précisé lorsque l'objet Anglais est saisi.")
    public boolean isCorpsEnObligatoireSiObjetEnRenseigne() {
        boolean hasObjet = objetEn != null && !objetEn.isBlank();
        boolean hasCorps = corpsEn != null && !corpsEn.isBlank();
        return !hasObjet || hasCorps;
    }

}
