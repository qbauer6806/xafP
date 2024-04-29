package mc.gouv.xaf.shared.enums;

public enum JobStatutsEnum {

    RUNNING("En cours d'exécution"),
    ERROR("Une erreur est survenue lors de l'exécution du job"),
    SUCCEEDED("Le job a été exécuté avec succès");

    private String libelle;

    private JobStatutsEnum(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }

}
