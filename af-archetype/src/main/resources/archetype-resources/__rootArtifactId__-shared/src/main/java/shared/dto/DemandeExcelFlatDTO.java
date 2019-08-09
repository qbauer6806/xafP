#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.dto;

import mc.gouv.dem.shared.model.DemandeFlatDTO;
import mc.gouv.${artifactIdLower}.shared.model.v1563199701514.ContenuProjectDemandeDTO;

public class DemandeExcelFlatDTO {

    public DemandeExcelFlatDTO(DemandeFlatDTO generic, ContenuProjectDemandeDTO contenu) {
        this.generic = generic;
        this.contenu = contenu;
    }

    private DemandeFlatDTO generic;

    private ContenuProjectDemandeDTO contenu;

    private SuiviComptableDTO suiviComptable;

    private CalculAideDTO calculAide;

    private String etatInterne;

    public DemandeFlatDTO getGeneric() {
        return generic;
    }

    public void setGeneric(DemandeFlatDTO generic) {
        this.generic = generic;
    }

    public ContenuProjectDemandeDTO getContenu() {
        return contenu;
    }

    public void setContenu(ContenuProjectDemandeDTO contenu) {
        this.contenu = contenu;
    }

    public SuiviComptableDTO getSuiviComptable() {
        return suiviComptable;
    }

    public void setSuiviComptable(SuiviComptableDTO suiviComptable) {
        this.suiviComptable = suiviComptable;
    }

    public CalculAideDTO getCalculAide() {
        return calculAide;
    }

    public void setCalculAide(CalculAideDTO calculAide) {
        this.calculAide = calculAide;
    }

    public String getEtatInterne() {
        return etatInterne;
    }

    public void setEtatInterne(String etatInterne) {
        this.etatInterne = etatInterne;
    }

}
