package mc.gouv.xaf.back.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.dao.DemandesStatistiquesInternesRepository;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.StatistiquesInternesService;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatistiquesInternesServiceImpl implements StatistiquesInternesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatistiquesInternesServiceImpl.class);

    @Autowired
    private DemandesStatistiquesInternesRepository demandesStatInternesRepository;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    private static final String TOTAL = "TOTAL";

    @Override
    public Map<String, Map<String, Long>> getNumberOfEachDemandes() {

        // Init global map and total by status
        Map<String, Map<String, Long>> map = new LinkedHashMap<>();
        Map<String, Long> totalByStatus = new HashMap<>();

        // Loop over Canals
        for (DemandeCanalEnum canal : DemandeCanalEnum.values()) {
            Map<String, Long> nbByStatus = new HashMap<>();
            map.put(canal.name(), nbByStatus);
            Long totalByCanal = 0L;

            // Loop over status and count total by canal
            for (String status : demarchesDataProvider.getStatusMap().keySet()) {
                Long count;
                count = getNumberDemandesFilteredByStatusAndCanal(canal.name(), status);
                totalByCanal += count;
                nbByStatus.put(status, count);
            }

            // Add total by canal to status map
            nbByStatus.put(TOTAL, totalByCanal);
        }

        // Compute total by status, this cannot be done at loop time because all number should be already computed
        computeTotalByStatus(map, totalByStatus);
        return map;
    }

    /**
     * Compute the totals for each status by canal
     * This is a post computing method and should be called when the entire map is already populated
     * @param map
     * @param totalByStatus
     */
    private void computeTotalByStatus(Map<String, Map<String, Long>> map, Map<String, Long> totalByStatus) {
        // Retrieve the status list and add a TOTAL line to compute the global total
        List<String> statusList = new ArrayList<>(demarchesDataProvider.getStatusMap().keySet());
        statusList.add(TOTAL);

        // Iterate over status and canal to compute each total
        for (String status : statusList) {
            Long totalByCanal = 0L;
            for (DemandeCanalEnum canal : DemandeCanalEnum.values()) {
                if (map.get(canal.name()) != null) {
                    totalByCanal += map.get(canal.name()).get(status);
                }
            }
            totalByStatus.put(status, totalByCanal);
        }
        map.put(TOTAL, totalByStatus);
    }

    @Override
    public Long getNumberDemandesFilteredByStatusAndCanal(String canal, String status) {

        LOGGER.info("Récupération du nombre de demandes par canal et status...");

        return demandesStatInternesRepository.countByCanalAndDernierStatutName(canal, status);
    }

    @Override
    public Long getNumberDemandesFilteredByStatusAndCanalWithIds(List<Integer> ids, String canal) {

        LOGGER.info("Récupération du nombre de demarches dans la liste ids...");

        return demandesStatInternesRepository.countByPkDemandesInAndCanal(ids, canal);
    }
}
