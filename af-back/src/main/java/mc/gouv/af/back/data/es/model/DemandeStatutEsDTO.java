package mc.gouv.af.back.data.es.model;

import mc.gouv.dem.shared.model.DemandeStatutDTO;

public class DemandeStatutEsDTO extends DemandeStatutDTO {

    private String libelleMotif;

    public String getLibelleMotif() {
        return libelleMotif;
    }

    public void setLibelleMotif(String libelleMotif) {
        this.libelleMotif = libelleMotif;
    }

}
