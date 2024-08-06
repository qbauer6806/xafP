package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire pour les usagers courrier
 * 
 * @author qdeme
 * 
 */
@Setter
@Getter
public class UsagerCourrierFormBean {

    private Integer titre;

    @Size(max = 50, message = "Maximum 50 lettres autorisées")
    private String nom;

    @Size(max = 20, message = "Maximum 20 lettres autorisées")
    private String prenom;

    @Size(max = 100, message = "La raison sociale doit avoir une taille comprise entre 0 et 100")
    private String raisonSociale;

    @NotEmpty
    @NotNull(message = "L’adresse doit être précisée")
    @Size(min = 1, max = 128, message = "L’adresse doit avoir une taille comprise entre 1 et 128")
    private String adresse1;

    @Size(max = 128, message = "Le complément d’adresse doit avoir une taille comprise entre 0 et 128")
    private String adresse2;

    @Size(max = 128, message = "Le complément d’adresse doit avoir une taille comprise entre 0 et 128")
    private String adresseComplement;

    @NotEmpty
    @NotNull(message = "Le code postal doit être précisé")
    @Size(min = 1, max = 10, message = "Le code postal doit avoir une taille comprise entre 1 et 10")
    private String codePostal;

    @NotEmpty
    @NotNull(message = "La ville doit être précisée")
    @Size(min = 1, max = 50, message = "La ville doit avoir une taille comprise entre 1 et 50")
    private String ville;

    @Size(max = 64, message = "Le numéro de téléphone doit avoir une taille comprise entre 0 et 64")
    private String telephone;

    @Size(max = 256, message = "L’adresse email doit avoir une taille comprise entre 0 et 256")
    private String email;

    @NotEmpty(message = "Le pays doit être précisé")
    private String paysChoisi;

}
