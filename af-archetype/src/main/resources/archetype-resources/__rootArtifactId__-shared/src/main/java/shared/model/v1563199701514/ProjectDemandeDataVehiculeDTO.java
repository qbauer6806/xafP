#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package mc.gouv.${artifactIdLower}.shared.model.v1563199701514;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeDataVehiculeDTO implements Serializable {

    private static final long serialVersionUID = 1563199701514L;
    private String nombrekm;
    private String datefacture;
    private String numeroimmat;
    private String marque;
    private String genre;
    private String datemiseencirculation;
    private VehiculeTypetousEnum type;
    private VehiculeTypesansimmatEnum typesansimmat;


    
    public String getNombrekm() {
        return nombrekm;
    }

    
    public void setNombrekm(String value) {
        this.nombrekm = value;
    }

    
    public String getDatefacture() {
        return datefacture;
    }

    
    public void setDatefacture(String value) {
        this.datefacture = value;
    }

    
    public String getNumeroimmat() {
        return numeroimmat;
    }

    
    public void setNumeroimmat(String value) {
        this.numeroimmat = value;
    }

    
    public String getMarque() {
        return marque;
    }

    
    public void setMarque(String value) {
        this.marque = value;
    }

    
    public String getGenre() {
        return genre;
    }

    
    public void setGenre(String value) {
        this.genre = value;
    }

    
    public String getDatemiseencirculation() {
        return datemiseencirculation;
    }

    
    public void setDatemiseencirculation(String value) {
        this.datemiseencirculation = value;
    }

    
    public VehiculeTypetousEnum getType() {
        return type;
    }

    
    public void setType(VehiculeTypetousEnum value) {
        this.type = value;
    }

    
    public VehiculeTypesansimmatEnum getTypesansimmat() {
        return typesansimmat;
    }

    
    public void setTypesansimmat(VehiculeTypesansimmatEnum value) {
        this.typesansimmat = value;
    }

}
