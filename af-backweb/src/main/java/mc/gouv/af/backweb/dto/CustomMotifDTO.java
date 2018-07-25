package mc.gouv.af.backweb.dto;

import mc.gouv.dem.shared.model.MotifDTO;

public class CustomMotifDTO extends MotifDTO {

    private String libelleFr;
    private String libelleEn;

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

}
