package mc.gouv.xaf.back.service.data;

import java.util.List;

public interface DemandesStatistiquesService {

    Long getNumberDemandesFilteredByStatusAndCanal(String demarcheId, String canal, String status);

    Long getNumberDemandesFilteredByStatusAndCanalWithIds(List<Integer> ids, String canal);
}
