package mc.gouv.xaf.shared.dto;

public class JustificatifTraitementDTO {
    private String historique;

    private String justification;

    public JustificatifTraitementDTO(String historique, String justification) {
        this.historique = historique;
        this.justification = justification;
    }

    public String getHistorique() {
        return historique;
    }

    public void setHistorique(String historique) {
        this.historique = historique;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }
}
