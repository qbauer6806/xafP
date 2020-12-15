package mc.gouv.xaf.shared.itg.resid.exception;

import mc.gouv.xaf.shared.itg.resid.dto.ResidErrorDTO;

import java.io.IOException;
import java.util.List;

public class ResidHttpResponseException extends IOException {

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
        return "ResidHttpResponseException{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", errors=" + errors +
                '}';
    }
}
