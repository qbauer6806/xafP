package mc.gouv.xaf.back.service;

import java.util.List;
import java.util.Map;

public interface StatistiquesInternesService {

    Map<String, Map<String, Long>> getNumberOfEachDemandes();

    Long getNumberDemandesFilteredByStatusAndCanal(String demarcheId, String canal, String status);

    Long getNumberDemandesFilteredByStatusAndCanalWithIds(List<Integer> ids, String canal);

}
