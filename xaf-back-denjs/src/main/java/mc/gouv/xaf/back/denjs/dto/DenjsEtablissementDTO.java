package mc.gouv.xaf.back.denjs.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO représentant un établissement scolaire
 *
 * @author qdeme
 */
@Setter
@Getter
public class DenjsEtablissementDTO {

    private String code;

    private String nom;

    private String nomPhrase;

    private String adresse;

    private String telephone;

    private String email;

}
