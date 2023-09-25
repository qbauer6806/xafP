package mc.gouv.xaf.shared.enums;

public enum StatutTachesEnum {

    VALIDER("Validée", "Validée par l'agent traitant", "Valider", "icon-icon_valide"),
    REFUSER("Refusée", "Refusée par l'agent traitant", "Refuser", "icon-icon_erreur"),
    RETOUR_GUICHET("Renvoi Guichet", "Renvoi Guichet", "Renvoyer au guichet", "icon-icon_usager");

    private final String libelle;

    private final String libelleValidation;

    private final String action;

    private final String icone;

    StatutTachesEnum(String libelle, String libelleValidation, String action, String icone) {
        this.libelle = libelle;
        this.libelleValidation = libelleValidation;
        this.action = action;
        this.icone = icone;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getLibelleValidation() {
        return libelleValidation;
    }

    public String getAction() {
        return action;
    }

    public String getIcone() {
        return icone;
    }
}
