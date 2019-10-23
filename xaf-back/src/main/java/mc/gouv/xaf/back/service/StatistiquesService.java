package mc.gouv.xaf.back.service;

import java.util.Map;

public interface StatistiquesService {

    Map<String, Map<String, Long>> getNumberOfEachDemandes();

}
