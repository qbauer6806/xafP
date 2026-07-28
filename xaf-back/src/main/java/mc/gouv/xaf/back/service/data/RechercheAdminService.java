package mc.gouv.xaf.back.service.data;

import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.model.RechercheCategoryDTO;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;

public interface RechercheAdminService {

    List<RechercheChampDTO> getRechercheChamps();

    List<RechercheCategoryDTO> getCategories();

    /**
     * <p>Enregistre les changements apportés à la page administration de la reecherche avancée.</p>
     * <p>Met à jour les propriétés de la recherche avancée.</p>
     *
     * @param rechercheChampDTOS
     *         objet contenant la liste des propriétés éditées
     */
    void updateRechercheChamps(List<RechercheChampDTO> rechercheChampDTOS);

    void deleteCategory(Integer id);

    List<RechercheCategoryDTO> updateCategories(List<RechercheCategoryDTO> categories);

    RechercheCategoryDTO addCategory(String label);

    String exportConfig() throws IOException;

    void importConfig(byte[] file) throws IOException;

    void refreshConfigs(JsonNode config, Map<String, String> rechercheAvancee);

}
