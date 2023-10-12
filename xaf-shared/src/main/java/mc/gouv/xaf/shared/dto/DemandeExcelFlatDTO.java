package mc.gouv.xaf.shared.dto;

import mc.gouv.xaf.shared.dto.es.GenericContenuDTO;

public class DemandeExcelFlatDTO {

    public DemandeExcelFlatDTO(DemandeFlatDTO generic, GenericContenuDTO contenu) {
        this.generic = generic;
        this.contenu = contenu;
    }

    private DemandeFlatDTO generic;

    private GenericContenuDTO contenu;

    private String etatInterne;

    public DemandeFlatDTO getGeneric() {
        return generic;
    }

    public void setGeneric(DemandeFlatDTO generic) {
        this.generic = generic;
    }

    public GenericContenuDTO getContenu() {
        return contenu;
    }

    public void setContenu(GenericContenuDTO contenu) {
        this.contenu = contenu;
    }

    public String getEtatInterne() {
        return etatInterne;
    }

    public void setEtatInterne(String etatInterne) {
        this.etatInterne = etatInterne;
    }

}
