package mc.gouv.xaf.back.paiement.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO regroupant les paramètres nécessaires à la création d'un facture dans CIR, configurable selon les données des démarches.
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
public class InformationFacturationDTO {

    private String nomTitulaire;

    private String prenomTitulaire;

    private String emailUsager;

}
