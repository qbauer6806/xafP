#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.dto;

import mc.gouv.dem.shared.model.DemandeFlatDTO;
import ${groupId}.shared.model.v1568884433537.ContenuProjectDemandeDTO;

public class DemandeExcelFlatDTO {

    public DemandeExcelFlatDTO(DemandeFlatDTO generic, ContenuProjectDemandeDTO contenu) {
        this.generic = generic;
        this.contenu = contenu;
    }

    private DemandeFlatDTO generic;

    private ContenuProjectDemandeDTO contenu;

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

    public String getEtatInterne() {
        return etatInterne;
    }

    public void setEtatInterne(String etatInterne) {
        this.etatInterne = etatInterne;
    }

}
