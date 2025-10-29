package mc.gouv.xaf.apiclient2tiers.dto;

/**
 * Classe pour modélisation JSON d'une réponse de WS
 *
 * @author qdeme
 *
 */
public class FileResponseDTO {

    /**
     * Message à retourner
     */
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
