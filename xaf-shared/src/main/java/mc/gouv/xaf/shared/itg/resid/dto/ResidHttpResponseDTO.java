package mc.gouv.xaf.shared.itg.resid.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResidHttpResponseDTO implements Serializable {

    private static final long serialVersionUID = 3891403896379854592L;

    private int httpStatus;

    private String message;

    private List<ResidErrorDTO> errors;

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<ResidErrorDTO> getErrors() {
        return errors;
    }

    public void setErrors(List<ResidErrorDTO> errors) {
        this.errors = errors;
    }

    public String toStringMessage() {
        String erreurs = "";
        for (ResidErrorDTO erreur : this.errors) {
            erreurs +=  "  - " + erreur.getClef() + " / " + erreur.getNom() + " / " + erreur.getLibelle() + "<br>";
        }
        return "Erreur " + httpStatus + " - " + message +" :<br>" + erreurs;
    }

    @Override
    public String toString() {
        return "ResidHttpResponseDTO{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}
