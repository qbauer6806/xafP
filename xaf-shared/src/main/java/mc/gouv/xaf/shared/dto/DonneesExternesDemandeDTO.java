package mc.gouv.xaf.shared.dto;

public class DonneesExternesDemandeDTO {

    private DemandeDTO demande;
    private DonneesExternesStatutRetourEnum statutRetour;
    private String source;

    public enum DonneesExternesStatutRetourEnum {
        OK,
        NOK,
        CONFLICT
    }

    public DemandeDTO getDemande() {
        return demande;
    }

    public void setDemande(DemandeDTO demande) {
        this.demande = demande;
    }

    public DonneesExternesStatutRetourEnum getStatut() {
        return statutRetour;
    }

    public void setStatut(DonneesExternesStatutRetourEnum statut) {
        this.statutRetour = statut;
    }

    public void setStatutRetour(DonneesExternesStatutRetourEnum statutRetour) {
        this.statutRetour = statutRetour;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
