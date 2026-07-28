package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Formulaire pour le preview des emails
 *
 * @author mboutelier.ext
 */
@Setter
@Getter
public class MotifPreviewFormBean {

    private String commentairePrerempli;

    private String texteAEnvoyer;

    private String codeMotifChoisi;

    @NotNull
    private Integer pkDemande;

}
