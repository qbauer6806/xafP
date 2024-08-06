package mc.gouv.xaf.shared.dto;

import lombok.Getter;
import lombok.Setter;

public class DonneesExternesDemandeDTO {

    @Setter
    @Getter
    private DemandeDTO demande;
    private DonneesExternesStatutRetourEnum statutRetour;
    @Setter
    @Getter
    private String source;

    public enum DonneesExternesStatutRetourEnum {
        OK,
        NOK,
        CONFLICT
    }

    public DonneesExternesStatutRetourEnum getStatut() {
        return statutRetour;
    }

    public void setStatut(DonneesExternesStatutRetourEnum statut) {
        this.statutRetour = statut;
    }

}
