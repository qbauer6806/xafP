package mc.gouv.xaf.back.data.dao;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import mc.gouv.xaf.back.data.entity.CacheBO;

/**
 * 
 * Repository pour les valeurs mises en cache en base de données.
 * 
 * Cf. commentaire de la classe CacheService afin d'en connaître l'utilité.
 * 
 * @author qdeme
 */
public interface CacheRepository extends CrudRepository<CacheBO, String> {

    // Avec gestion des accès concurrents + @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CacheBO c WHERE c.pkCache = :id")
    CacheBO findByIdForUpdate(@Param("id") String id);
    
}
