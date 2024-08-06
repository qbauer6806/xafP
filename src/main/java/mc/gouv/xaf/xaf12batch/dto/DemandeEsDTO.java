package mc.gouv.xaf.xaf12batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DemandeEsDTO {
    private String _class;
    private DemandeStatutEsDTO dernierStatut;

    private Integer pkDemandes;

    public DemandeStatutEsDTO getDernierStatut() {
        return dernierStatut;
    }

    public void setDernierStatut(DemandeStatutEsDTO dernierStatut) {
        this.dernierStatut = dernierStatut;
    }

    public Integer getPkDemandes() {
        return pkDemandes;
    }

    public void setPkDemandes(Integer pkDemandes) {
        this.pkDemandes = pkDemandes;
    }

    public String get_class() {
        return _class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }
}
