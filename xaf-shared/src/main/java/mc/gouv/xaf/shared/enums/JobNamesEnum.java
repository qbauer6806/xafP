package mc.gouv.xaf.shared.enums;

import lombok.Getter;

@Getter
public enum JobNamesEnum {
    TRAITEMENT_DEAD_LETTER_TOPIC_GU_KAFKA("Traitement du Dead Letter Topic du Guichet Unique sur Kafka"),
    TRAITEMENT_OUTBOX_KAFKA("Traitement de l'Outbox Kafka"),
    SYNCHRONISATION_GLOBALE_GU("Synchronisation globale des demandes des usagers avec le Guichet Unique"),
    RECUPERATION_NOMBRE_MESSAGES_OUTBOX_KAFKA("Récupération du nombre de messages contenus dans l'Outbox Kafka"),
    RESET_MARQUEURS("Reset des marqueurs : suppression et génération");

    private final String libelle;

    JobNamesEnum(String libelle) {
        this.libelle = libelle;
    }

    public static JobNamesEnum getByName(String name) {
        if (name == null) {
            return null;
        }
        for (JobNamesEnum jobNameEnum : values()) {
            if (jobNameEnum.name().equals(name)) {
                return jobNameEnum;
            }
        }

        return null;

    }

}
