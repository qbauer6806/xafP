package mc.gouv.xaf.shared.formbean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MotifFormBean {

    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code;

    @NotEmpty(message = "Le libellé doit être précisé")
    @Size(max = 256, message = "Le libellé doit avoir une taille comprise entre 0 et 256")
    private String libelle;

    @Size(max = 5000, message = "Le commentaire prérempli doit avoir une taille comprise entre 0 et 5000")
    private String commentairePrerempli;

    @Size(max = 5000, message = "Le texte à envoyer doit avoir une taille comprise entre 0 et 5000")
    private String texteAEnvoyer;

    private String statut;

    private String langue;

}
