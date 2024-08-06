package mc.gouv.xaf.back.paiement.dto.itg.cir;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PermisDTO {

    private String numPermis;

    private String nom;

    private String prenom;

    private String adresse1;

    private String adresse2;

    private String nationalite;

    private String villeNaissance;

    private String dateNaissance;

}
