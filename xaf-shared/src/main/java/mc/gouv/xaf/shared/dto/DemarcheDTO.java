package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Modélise le contenu d'une démarche
 *
 * @author qdeme
 */
@Setter
@Getter
public class DemarcheDTO {

    private String pkDemarches;

    private String nom;

    private String nomEn;

    private String emailService;

    private String emailReplyto;

    private String emailReplytoNom;

    private String emailFrom;

    private String emailFromNom;

    private String identifiantPrefixe;

    private String langues;

    private String nomDirection;
    private String nomSousDirection;
    private String nomFooter;
    private String adresseService;
    private String nomSousDirectionComplement;
    private String telephoneService;
    private String nomDirectionEn;
    private String nomSousDirectionEn;
    private String nomSousDirectionComplementEn;

}
