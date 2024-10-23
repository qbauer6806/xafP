package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.shared.enums.StatutSimplifieEnum;

public enum DemandeStatutEnum {

    EN_ATTENTE_TRAIT("En attente de traitement", false, StatutSimplifieEnum.EN_COURS),
    EN_COURS_TRAIT("En cours de traitement", false, StatutSimplifieEnum.EN_COURS),
    EN_ATTENTE_COMPL("En attente d'informations complémentaires", true, StatutSimplifieEnum.EN_COURS),
    EN_ATTENTE_DE_PAIEMENT("En attente de paiement", true, StatutSimplifieEnum.EN_COURS),
    EN_ATTENTE_RETRAIT_OU_ECHANGE("En attente d’échange ou de retrait du permis monégasque", false,
            StatutSimplifieEnum.EN_COURS),
    CLOTUREE("Clôturée", true, StatutSimplifieEnum.TERMINEE),
    REFUSEE("Refusée", true, StatutSimplifieEnum.TERMINEE),
    ANNULEE("Annulée", true, StatutSimplifieEnum.TERMINEE);

    public String libelle;

    public boolean containsMotifs;

    public StatutSimplifieEnum statutSimplifie;

    DemandeStatutEnum(String libelle, boolean containsMotifs, StatutSimplifieEnum statutSimplifie) {
        this.libelle = libelle;
        this.containsMotifs = containsMotifs;
        this.statutSimplifie = statutSimplifie;
    }

    @Override
    public String toString() {
        return libelle;
    }

}
