package mc.gouv.xaf.back.data.entity;

import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import tools.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Classe BO de la table DEM.CACHE
 * 
 * Cf. commentaire de la classe CacheService afin d'en connaître l'utilité.

 * @author qdeme
 */
@Setter
@Getter
@Entity
@Table(name = "DEM_CACHE")
public class CacheBO {

    @Id
    @Column(name = "PK_CACHE", nullable = false)
    private String pkCache;
    
    @Column(name = "DATA", columnDefinition = "JSONB", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode data;

    @Column(name = "DATE_MAJ", nullable = false)
    private Date dateMaj;

}
