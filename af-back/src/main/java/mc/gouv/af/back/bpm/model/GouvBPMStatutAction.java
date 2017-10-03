package mc.gouv.af.back.bpm.model;

import mc.gouv.dem.shared.model.DemandeStatutEnum;

/**
 * Classe permettant de représenter une association entre action et son statut
 * cible
 * 
 * @author qdeme
 *
 */
public class GouvBPMStatutAction {

    private DemandeStatutEnum statut;

    private String action;

    public GouvBPMStatutAction(DemandeStatutEnum statut, String action) {
        this.statut = statut;
        this.action = action;
    }

    public DemandeStatutEnum getStatut() {
        return statut;
    }

    public void setStatut(DemandeStatutEnum statut) {
        this.statut = statut;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
