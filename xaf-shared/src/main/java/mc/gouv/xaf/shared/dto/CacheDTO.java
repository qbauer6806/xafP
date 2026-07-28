package mc.gouv.xaf.shared.dto;

import java.util.Date;

import tools.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO représentant une valeur mise en cache en base de données.
 * 
 * Cf. commentaire de la classe CacheService afin d'en connaître l'utilité.

 * @author qdeme
 */
@Setter
@Getter
public class CacheDTO {

    private String pkCache;
    
    private JsonNode data;
    
    private Date dateMaj;
    
}
