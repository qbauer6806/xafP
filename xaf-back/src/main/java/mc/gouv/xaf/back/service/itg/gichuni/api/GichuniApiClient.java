package mc.gouv.xaf.back.service.itg.gichuni.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
	private RestTemplate restTemplate;

	public GichuniUsagerDTO getUsager(Integer id) {
		GichuniUsagerDTO[] usagers = restTemplate.getForObject(
				gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + id,
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
			if (!builder.isEmpty()) {
				builder.append(',');
			}
			builder.append(id);
		}

		GichuniUsagerDTO[] usagers = restTemplate.getForObject(
				gouvPropertiesResolver.getGichuniUrl() + "/profiles/profile-ids/" + builder,
				GichuniUsagerDTO[].class);

		if (usagers == null || usagers.length == 0) {
			return new ArrayList<>();
		}
		return Arrays.asList(usagers);
	}
}
