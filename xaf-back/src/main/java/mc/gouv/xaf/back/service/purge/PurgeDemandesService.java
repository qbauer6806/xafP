package mc.gouv.xaf.back.service.purge;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface PurgeDemandesService {

	void purgerDemandesDansStatuts(List<String> statuts, int jours) throws JsonProcessingException;
}
