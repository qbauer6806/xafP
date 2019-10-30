package mc.gouv.xaf.backweb.dto;

import mc.gouv.xaf.shared.dto.MotifDTO;

public class CustomMotifDTO extends MotifDTO {

    private String libelleFr;
    private String libelleEn;

    private String commentairePrerempliFr;
    private String commentairePrerempliEn;

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

}
