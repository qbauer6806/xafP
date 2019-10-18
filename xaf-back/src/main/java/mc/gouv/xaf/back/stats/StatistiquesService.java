package mc.gouv.xaf.back.stats;

import java.util.Map;

public interface StatistiquesService {

    Map<String, Map<String, Long>> getNumberOfEachDemandes();

}
