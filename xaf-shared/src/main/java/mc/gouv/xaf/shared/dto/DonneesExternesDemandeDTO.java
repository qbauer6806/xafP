package mc.gouv.xaf.shared.dto;

public class DonneesExternesDemandeDTO {

    private DemandeDTO demande;
    private Integer statut;

    public DemandeDTO getDemande() {
        return demande;
    }

    public void setDemande(DemandeDTO demande) {
        this.demande = demande;
    }

    public Integer getStatut() {
        return statut;
    }

    public void setStatut(Integer statut) {
        this.statut = statut;
    }

}
