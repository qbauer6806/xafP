#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.shared.model.v1568884433537;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectDemandeFieldDonneeDerogationJoursferiesDTO implements Serializable {

    private static final long serialVersionUID = 1568884433537L;
    private boolean jourDeL_An;
    private boolean sainteDevote;
    private boolean immaculeeConception;
    private boolean noel;
    private boolean lundiDePaques;
    private boolean le1erMai;
    private boolean ascension;
    private boolean lundiDePentecote;
    private boolean feteDieu;
    private boolean assomption;
    private boolean toussaint;
    private boolean feteDuPrince;


    
    public boolean getJourDeL_An() {
        return jourDeL_An;
    }

    
    public void setJourDeL_An(boolean value) {
        this.jourDeL_An = value;
    }

    
    public boolean getSainteDevote() {
        return sainteDevote;
    }

    
    public void setSainteDevote(boolean value) {
        this.sainteDevote = value;
    }

    
    public boolean getImmaculeeConception() {
        return immaculeeConception;
    }

    
    public void setImmaculeeConception(boolean value) {
        this.immaculeeConception = value;
    }

    
    public boolean getNoel() {
        return noel;
    }

    
    public void setNoel(boolean value) {
        this.noel = value;
    }

    
    public boolean getLundiDePaques() {
        return lundiDePaques;
    }

    
    public void setLundiDePaques(boolean value) {
        this.lundiDePaques = value;
    }

    
    public boolean getLe1erMai() {
        return le1erMai;
    }

    
    public void setLe1erMai(boolean value) {
        this.le1erMai = value;
    }

    
    public boolean getAscension() {
        return ascension;
    }

    
    public void setAscension(boolean value) {
        this.ascension = value;
    }

    
    public boolean getLundiDePentecote() {
        return lundiDePentecote;
    }

    
    public void setLundiDePentecote(boolean value) {
        this.lundiDePentecote = value;
    }

    
    public boolean getFeteDieu() {
        return feteDieu;
    }

    
    public void setFeteDieu(boolean value) {
        this.feteDieu = value;
    }

    
    public boolean getAssomption() {
        return assomption;
    }

    
    public void setAssomption(boolean value) {
        this.assomption = value;
    }

    
    public boolean getToussaint() {
        return toussaint;
    }

    
    public void setToussaint(boolean value) {
        this.toussaint = value;
    }

    
    public boolean getFeteDuPrince() {
        return feteDuPrince;
    }

    
    public void setFeteDuPrince(boolean value) {
        this.feteDuPrince = value;
    }

}
