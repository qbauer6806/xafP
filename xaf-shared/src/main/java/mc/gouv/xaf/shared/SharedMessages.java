package mc.gouv.xaf.shared;

public class SharedMessages {

    public static final String UNSAFE_CHARS = "[\n\r]";
    public static final String SUCCESS_MESSAGES = "successMessages";
    public static final String ERROR_MESSAGES = "errorMessages";
    public static final String TRANSFORMATION_BO_DTO = "Transformation bo -> dto ...";
    public static final String TRANSFORMATION_DTO_BO = "Transformation dto -> bo ...";
    public static final String RECUPERATION_EN_BASE = "Récupération en base...";
    public static final String SAUVEGARDE_EN_BASE = "Sauvegarde en base...";
    public static final String DONNEE_INTROUVABLE = "La donnée recherchée est introuvable";
    public static final String DEMANDE_ASSOCIEE_INTROUVABLE = "La demande associée est introuvable";
    public static final String APPEL_SAVE_HISTORIQUE = "Appel au service de sauvegarde de l'historique...";
    public static final String ERREUR_HISTORIQUE = "Erreur lors de la création de l'historique {}";
    public static final String CLAIM_AND_SUBMIT_TASK = "claimTask() puis submitTaskFormData()...";
    public static final String UTILISATEUR_NON_AUTORISE = "Utilisateur non autorisé";
    public static final String ERREUR_INDEXATION = "Erreur lors de l'indexation de la demande.";
    public static final String REQUETE_MALFORMEE = "Requête malformée";
    public static final String TROP_DE_FICHIERS = "Trop de fichiers envoyés dans la requête";
    public static final String FICHIER_TYPE_EXTENTION_INVALIDE = "Le type/extension du fichier soumis n'est pas valide";
    public static final String FICHIER_NOM_MANQUANT = "Nom du fichier manquant";
    public static final String FICHIER_TROP_GRAND = "La taille du fichier est trop grande";
    public static final String FICHIER_LIMITE_UPLOAD_ATTEINTE = "La limite de nombre de fichiers uploadés a été atteinte";
    public static final String ERREUR_INTERNE = "Erreur interne lors du traitement";
    public static final String DEFAULT_TITRE_MAIL_FR = "Madame, Monsieur";
    public static final String DEFAULT_TITRE_MAIL_EN = "Madam, Sir";

    private SharedMessages() {
    }

}
