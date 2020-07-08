package mc.gouv.xaf.back.bpm.model;

/**
 * Classe permettant de représenter une association entre action et son statut
 * cible
 * 
 * @author qdeme
 *
 */
public class GouvBPMStatutAction {

    private String statut;

    private String action;

    public GouvBPMStatutAction(String statut, String action) {
        this.statut = statut;
        this.action = action;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
