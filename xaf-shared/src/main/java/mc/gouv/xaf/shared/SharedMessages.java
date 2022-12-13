package mc.gouv.xaf.shared;

public class SharedMessages {

    public static final String UNSAFE_CHARS = "[\n\r\t]";

    public static final String SUCCESS_MESSAGES = "successMessages";
    public static final String ERROR_MESSAGES = "errorMessages";
    public static final String TRANSFORMATION_BO_DTO = "Transformation bo -> dto ...";
    public static final String TRANSFORMATION_DTO_BO = "Transformation dto -> bo ...";
    public static final String RECUPERATION_EN_BASE = "Récupération en base...";
    public static final String SAUVEGARDE_EN_BASE = "Sauvegarde en base...";
    public static final String DONNEE_INTROUVABLE = "La donée recherchée est introuvable";
    public static final String DEMANDE_ASSOCIEE_INTROUVABLE = "La demande associée est introuvable";

    public static final String APPEL_SAVE_HISTORIQUE = "Appel au service de sauvegarde de l'historique...";
    public static final String ERREUR_HISTORIQUE = "Erreur lors de la création de l'historique {}";

    public static final String CLAIM_AND_SUBMIT_TASK = "claimTask() puis submitTaskFormData()...";

    private SharedMessages() {
    }

}
