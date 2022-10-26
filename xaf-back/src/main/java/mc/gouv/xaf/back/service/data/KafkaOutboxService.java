package mc.gouv.xaf.back.service.data;

import java.util.List;

import mc.gouv.xaf.shared.dto.KafkaOutboxDTO;

/**
 * 
 * Service permettant la manipulation de l'outbox direction Kafka
 *
 * @author qdeme
 * 
 */
public interface KafkaOutboxService {
	
	public List<KafkaOutboxDTO> getOutboxElements();
	
	public KafkaOutboxDTO getOutboxElement(Integer pkOutboxElement);
	
	public KafkaOutboxDTO createOutboxElement(KafkaOutboxDTO outboxElement);
	
	public KafkaOutboxDTO updateOutboxElement(KafkaOutboxDTO outboxElement);
	
	public void deleteOutboxElement(Integer pkGUKafkaOutbox);
	
	public Integer resetAllOutboxElements();

	public Integer getNbOutboxElements();

}
