package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.bpm.GouvBPM;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.StatistiquesService;
import mc.gouv.xaf.back.service.data.DemandesStatistiquesService;
import mc.gouv.xaf.back.shared.dto.DemandeCanalEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class StatistiquesServiceImpl implements StatistiquesService {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private DemandesStatistiquesService demandesStatistiquesService;

    @Autowired
    private GouvBPM gouvBPM;

    private static final String TOTAL = "TOTAL";

    @Override
    public Map<String, Map<String, Long>> getNumberOfEachDemandes() {

        String demarcheId = gouvPropertiesResolver.getDemarcheId();

        // Private status
        Map<String, String> privateStatusMap = demarchesDataProvider.getPrivateStatusMap();

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
                // If a public status retrieve it from db, else from bpm
                if (!privateStatusMap.containsKey(status)) {
                    count = demandesStatistiquesService.getNumberDemandesFilteredByStatusAndCanal(demarcheId, canal.name(), status);
                    totalByCanal += count;
                }
                else {
                    List<String> tasksIds = gouvBPM.getNumberActiveDemandesInState(status);

                    // Convert String id list to Integer id list
                    List<Integer> taskIntIds = tasksIds.stream().map(Integer::parseInt).collect(Collectors.toList());
                    count = demandesStatistiquesService.getNumberDemandesFilteredByStatusAndCanalWithIds(taskIntIds, canal.name());
                }
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
}
