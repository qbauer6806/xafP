package mc.gouv.xaf.shared.formbean;

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
public class MotifCreateFormBean {

    @NotEmpty(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    @Pattern(
            regexp = "^[A-Z_]+$",
            message = "Le code doit contenir uniquement des majuscules et des underscores"
    )
    private String code;

    @NotEmpty(message = "Le libellé doit être précisé")
    @Size(max = 256, message = "Le libellé Français doit avoir une taille comprise entre 0 et 256")
    private String libelleFr;

    @NotEmpty(message = "Le statut cible doit être précisé")
    private String statut;

    @Size(max = 5000, message = "Le commentaire prérempli Français doit avoir une taille comprise entre 0 et 5000")
    private String commentairePrerempliFr;

    @Size(max = 5000, message = "Le texte à envoyer Français doit avoir une taille comprise entre 0 et 5000")
    private String texteAEnvoyerFr;

    @Size(max = 256, message = "Le libellé Anglais doit avoir une taille comprise entre 0 et 256")
    private String libelleEn;

    @Size(max = 5000, message = "Le commentaire prérempli Anglais doit avoir une taille comprise entre 0 et 5000")
    private String commentairePrerempliEn;

    @Size(max = 5000, message = "Le texte à envoyer Anglais doit avoir une taille comprise entre 0 et 5000")
    private String texteAEnvoyerEn;

}
