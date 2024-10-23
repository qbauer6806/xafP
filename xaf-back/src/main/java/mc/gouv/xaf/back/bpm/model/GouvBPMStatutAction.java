package mc.gouv.xaf.back.bpm.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Classe permettant de représenter une association entre action et son statut cible
 *
 * @author qdeme
 */
@Setter
@Getter
public class GouvBPMStatutAction {

    private String statut;

    private String action;

    public GouvBPMStatutAction(String statut, String action) {
        this.statut = statut;
        this.action = action;
    }

}
