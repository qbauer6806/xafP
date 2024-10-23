package mc.gouv.xaf.backweb.formbean;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * Formulaire de la page de gestion des motifs
 *
 * @author tverdoyan
 */
@Getter
public class MotifsFormBean {

    @Setter
    @NotEmpty
    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code = null;

    @Setter
    @NotEmpty
    @NotNull(message = "Le libellé en Français doit être précisé")
    @Size(min = 3, max = 256, message = "Le libellé en Français doit avoir une taille comprise entre 3 et 256")
    private String libelleFr;

    @Setter
    @Size(max = 256, message = "Le libellé en Anglais doit avoir une taille comprise entre 0 et 256")
    private String libelleEn;

    @Setter
    @Size(max = 1500, message = "Le commentaire en Français doit avoir une taille comprise entre 0 et 1500")
    private String commentairePrerempliFr;

    @Setter
    @Size(max = 1500, message = "Le commentaire en Anglais doit avoir une taille comprise entre 0 et 1500")
    private String commentairePrerempliEn;

    @Setter
    private String texteAEnvoyerFr;
    @Setter
    private String texteAEnvoyerEn;

    @Setter
    private String statut;

    @Setter
    private String statutEnum;
    @Setter
    private int enumOrdinalVal;
    @Setter
    private String fieldsetErrTitle;
    @Setter
    private Integer motifPkFr;
    @Setter
    private Integer motifPkEn;
    private String codeVisible;
    @Setter
    private Integer hashCode;
    @Setter
    private String errorMsg;
    @Setter
    private boolean errCodeExiste;
    @Setter
    private boolean errGlobale;
    @Setter
    private String dateArchive;

    public void setCodeVisible(String codeVisible) {
        if (StringUtils.isNotBlank(codeVisible)) {
            this.code = codeVisible;
        }
        this.codeVisible = codeVisible;
    }

    public Boolean isMotifActif() {
        return dateArchive == null || dateArchive.length() <= 0;
    }

}
