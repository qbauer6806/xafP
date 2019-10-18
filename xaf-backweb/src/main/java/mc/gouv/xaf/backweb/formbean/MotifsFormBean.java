package mc.gouv.xaf.backweb.formbean;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Formulaire de la page de gestion des motifs
 * 
 * @author tverdoyan
 * 
 */
public class MotifsFormBean {

    @NotBlank
    @NotNull(message = "Le code doit être précisé")
    @Size(min = 3, max = 128, message = "Le code doit avoir une taille comprise entre 3 et 128")
    private String code = null;

    @NotBlank
    @NotNull(message = "Le libellé en Français doit être précisé")
    @Size(min = 3, max = 256, message = "Le libellé en Français doit avoir une taille comprise entre 3 et 256")
    private String libelleFr;

    @Size(min = 0, max = 256, message = "Le libellé en Anglais doit avoir une taille comprise entre 0 et 256")
    private String libelleEn;

    private String commentairePrerempliFr;
    private String commentairePrerempliEn;

    private String texteAEnvoyerFr;
    private String texteAEnvoyerEn;

    private String statut;

    private String statutEnum;
    private int enumOrdinalVal;
    private String fieldsetErrTitle;
    private Integer motifPkFr;
    private Integer motifPkEn;
    private String codeVisible;
    private Integer hashCode;
    private String errorMsg;
    private boolean isErrCodeExiste;
    private boolean isErrGlobale;
    private String dateArchive;

    public boolean getIsErrGlobale() {
        return isErrGlobale;
    }

    public void setIsErrGlobale(boolean isErrGlobale) {
        this.isErrGlobale = isErrGlobale;
    }

    public boolean getIsErrCodeExiste() {
        return isErrCodeExiste;
    }

    public void setIsErrCodeExiste(boolean isErrCodeExiste) {
        this.isErrCodeExiste = isErrCodeExiste;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(Integer hashCode) {
        this.hashCode = hashCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getCodeVisible() {
        return codeVisible;
    }

    public void setCodeVisible(String codeVisible) {
        if (StringUtils.isNotBlank(codeVisible)) {
            this.code = codeVisible;
        }
        this.codeVisible = codeVisible;
    }

    public Boolean isMotifActif() {
        return dateArchive == null || dateArchive.length() <= 0;
    }

    public String getDateArchive() {
        return dateArchive;
    }

    public void setDateArchive(String dateArchive) {
        this.dateArchive = dateArchive;
    }

    public Integer getMotifPkFr() {
        return motifPkFr;
    }

    public void setMotifPkFr(Integer motifPkFr) {
        this.motifPkFr = motifPkFr;
    }

    public Integer getMotifPkEn() {
        return motifPkEn;
    }

    public void setMotifPkEn(Integer motifPkEn) {
        this.motifPkEn = motifPkEn;
    }

    public String getFieldsetErrTitle() {
        return fieldsetErrTitle;
    }

    public void setFieldsetErrTitle(String fieldsetErrTitle) {
        this.fieldsetErrTitle = fieldsetErrTitle;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelleFr() {
        return libelleFr;
    }

    public void setLibelleFr(String libelleFr) {
        this.libelleFr = libelleFr;
    }

    public String getLibelleEn() {
        return libelleEn;
    }

    public void setLibelleEn(String libelleEn) {
        this.libelleEn = libelleEn;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getStatutEnum() {
        return statutEnum;
    }

    public void setStatutEnum(String statutEnum) {
        this.statutEnum = statutEnum;
    }

    public int getEnumOrdinalVal() {
        return enumOrdinalVal;
    }

    public void setEnumOrdinalVal(int enumOrdinalVal) {
        this.enumOrdinalVal = enumOrdinalVal;
    }

    public String getCommentairePrerempliFr() {
        return commentairePrerempliFr;
    }

    public void setCommentairePrerempliFr(String commentairePrerempliFr) {
        this.commentairePrerempliFr = commentairePrerempliFr;
    }

    public String getCommentairePrerempliEn() {
        return commentairePrerempliEn;
    }

    public void setCommentairePrerempliEn(String commentairePrerempliEn) {
        this.commentairePrerempliEn = commentairePrerempliEn;
    }

    public String getTexteAEnvoyerFr() {
        return texteAEnvoyerFr;
    }

    public void setTexteAEnvoyerFr(String texteAEnvoyerFr) {
        this.texteAEnvoyerFr = texteAEnvoyerFr;
    }

    public String getTexteAEnvoyerEn() {
        return texteAEnvoyerEn;
    }

    public void setTexteAEnvoyerEn(String texteAEnvoyerEn) {
        this.texteAEnvoyerEn = texteAEnvoyerEn;
    }
}
