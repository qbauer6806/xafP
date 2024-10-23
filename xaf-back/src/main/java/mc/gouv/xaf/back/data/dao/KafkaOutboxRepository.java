package mc.gouv.xaf.back.data.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import mc.gouv.xaf.back.data.entity.KafkaOutboxBO;

/**
 * @author qdeme
 */
public interface KafkaOutboxRepository extends CrudRepository<KafkaOutboxBO, Integer> {

    @Override
    List<KafkaOutboxBO> findAll();

}
