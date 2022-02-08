package mc.gouv.xaf.back.service.itg.gichuni.api;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;

import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;

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
	
    public UsagerBean getUsager(Integer id) {
    	UsagerBean[] usagers = restTemplate.getForObject(gouvPropertiesResolver.getGichuniUrl() + "/profiles/" + id,
				UsagerBean[].class);
    	if (usagers == null || usagers.length == 0) {
    		return null;
    	}
    	return usagers[0];
    }
    
    public List<UsagerBean> getUsagers(List<Integer> ids) {
	    // Concaténation des ids fournis pour le WS
	    String listId = null;
	    for (Integer id : ids) {
	        if (listId == null) {
	            listId = id.toString();
	        }
	        else {
	            listId += "," + id.toString();
	        }
	    }
	    
    	UsagerBean[] usagers = restTemplate.getForObject(gouvPropertiesResolver.getGichuniUrl() + "/profiles/" + listId,
				UsagerBean[].class);
    	return Arrays.asList(usagers);
    }
}
