package mc.gouv.xaf.back.service.itg.gichuni.kafka;

import java.util.Date;

/**
 * 
 * Service permettant la production de messages pour le Guichet Unique via Kafka
 * Les signatures des méthodes sont dépendantes de la version des messages envoyés, mais on établit la règle que les messages
 * envoyés sont toujours à la version la plus élevée possible.
 * Donc ce service n'est pas placé dans un package type "v1" ou "v2"... Les signatures évolueront au fur et à mesure que les versions
 * de messages évolueront.
 * 
 * @author qdeme
 *
 */
public interface GUKafkaProducer {

	void sendCreationDemandeMessage(Integer usagerId, Integer demandeId, String identifiant, Date dateCreation, Integer nbDemandesUsager);

	void sendDesinscriptionUsagerTSMessage(Integer usagerId);

}
