package mc.gouv.xaf.back.service;

import tools.jackson.databind.JsonNode;
import java.util.Map;

public interface DonneesExternesService {

    JsonNode getDonneesExternes(Integer usagerId, Map<String, String[]> params);
    
}
