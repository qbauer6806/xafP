package mc.gouv.xaf.front.util;

import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * Cache très simpliste basé uniquement sur la durée d'expiration des données dans leur ensemble.
 * En l'occurrence, les FrontProperties.
 * 
 * @author qdeme
 *
 */
@Component
public class FrontControllerPropertiesCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(FrontControllerPropertiesCache.class);

	static List<PropertiesDTO> properties = null;

	static Long lastRefresh = null;

	// Rafraîchir le cache si les propriétés sont plus anciennes que 60 secondes
	static final long DUREE_EXPIRATION_MS = 60000;

	@Autowired
	private XafFrontserverUtils xafFrontserverUtils;

	private FrontControllerPropertiesCache()  {
	}
	
	public List<PropertiesDTO> getFrontProperties() {
		if (properties == null || (lastRefresh == null || (System.currentTimeMillis() - lastRefresh) > DUREE_EXPIRATION_MS)) {
			LOGGER.info("Expiration des FrontProperties, récupération depuis l'API...");
			properties = xafFrontserverUtils.getAfApiClient().getFrontProperties();
			lastRefresh = System.currentTimeMillis();
		}
		return properties;
	}
	
	public PropertiesDTO getFrontProperty(String key) {
		List<PropertiesDTO> propFiltrees = getFrontProperties().stream().filter(prop -> prop.getKey().equals(key)).collect(Collectors.toList());
        return (!propFiltrees.isEmpty()) ? propFiltrees.get(0) : null;
	}

}
