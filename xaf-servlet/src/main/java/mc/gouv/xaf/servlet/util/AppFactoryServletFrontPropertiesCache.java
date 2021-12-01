package mc.gouv.xaf.servlet.util;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * 
 * Cache très simpliste basé uniquement sur la durée d'expiration des données dans leur ensemble.
 * En l'occurrence, les FrontProperties.
 * 
 * @author qdeme
 *
 */
public class AppFactoryServletFrontPropertiesCache {
	
	private static Logger LOGGER = LoggerFactory.getLogger(AppFactoryServletFrontPropertiesCache.class);
	
	static List<PropertiesDTO> properties = null;
	
	static Long lastRefresh = null;
	
	// Rafraîchir le cache si les propriétés sont plus anciennes que 10 secondes
	static final long dureeExpirationMs = 10000; 
	
	public static List<PropertiesDTO> getFrontProperties() {
		if (properties == null || (lastRefresh == null || (System.currentTimeMillis() - lastRefresh) > dureeExpirationMs)) {
			LOGGER.info("Expiration des FrontProperties, récupération depuis l'API...");
			properties = AppFactoryServletUtils.getAfApiClient().getFrontProperties();
			lastRefresh = System.currentTimeMillis();
		}
		return properties;
	}
	
	public static PropertiesDTO getFrontProperty(String key) {
		List<PropertiesDTO> propFiltrees = getFrontProperties().stream().filter(prop -> prop.getKey().equals(key)).collect(Collectors.toList());
        return (propFiltrees.size() > 0) ? propFiltrees.get(0) : null;
	}

}
