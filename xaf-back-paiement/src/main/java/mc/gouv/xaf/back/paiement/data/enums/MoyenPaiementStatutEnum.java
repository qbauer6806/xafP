package mc.gouv.xaf.back.paiement.data.enums;


public enum MoyenPaiementStatutEnum {
    INVALIDE("INVALID"),
    VALIDE("VALID"),
    ENREGISTRE_A_LA_CREATION("ENREGISTRE_A_LA_CREATION"),
    EN_ATTENTE_DE_VALIDATION("EN_ATTENTE_DE_VALIDATION");

    private String mwpaymtValue;

    MoyenPaiementStatutEnum(String libelle) {
        this.mwpaymtValue = libelle;
    }

    public static MoyenPaiementStatutEnum fromLibelle(String libelle) {
        for (MoyenPaiementStatutEnum statut : values()) {
            if (statut.mwpaymtValue.equals(libelle)) {
                return statut;
            }
        }
        throw new IllegalArgumentException("Aucun statut trouvé pour : " + libelle);
    }
}
