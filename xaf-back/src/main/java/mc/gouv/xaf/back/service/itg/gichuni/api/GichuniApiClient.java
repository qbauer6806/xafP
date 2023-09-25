package mc.gouv.xaf.back.service.itg.gichuni.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

/**
 * 
 * Classe permettant d'appeler l'API GICHUNI
 * 
 * @author qdeme
 * 
 */
@Service
public class GichuniApiClient {

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;
	
	@Autowired
	private OAuth2RestTemplate restTemplate;
	
    public GichuniUsagerDTO getUsager(Integer id) {
    	GichuniUsagerDTO[] usagers = restTemplate.getForObject(gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + id,
    			GichuniUsagerDTO[].class);
    	if (usagers == null || usagers.length == 0) {
    		return null;
    	}
    	return usagers[0];
    }
    
    public List<GichuniUsagerDTO> getUsagers(List<Integer> ids) {
	    // Concaténation des ids fournis pour le WS
		StringBuilder builder = new StringBuilder();
	    for (Integer id : ids) {
	    	if (builder.length() != 0) {
	    		builder.append(',');
			}
	    	builder.append(id);
	    }
	    
    	GichuniUsagerDTO[] usagers = restTemplate.getForObject(gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + builder,
    			GichuniUsagerDTO[].class);

		if (usagers == null || usagers.length == 0) {
			return new ArrayList<>();
		}
    	return Arrays.asList(usagers);
    }
}
