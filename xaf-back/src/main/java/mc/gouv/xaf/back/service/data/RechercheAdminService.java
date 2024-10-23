package mc.gouv.xaf.back.service.data;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.model.RechercheCategoryDTO;
import mc.gouv.xaf.back.data.model.RechercheChampDTO;

public interface RechercheAdminService {

    Map<String, RechercheChampConfigBO> getChampsMap();

    List<RechercheChampDTO> getRechercheChamps();

    List<RechercheCategoryDTO> getCategories();

    RechercheCategoryDTO updateCategory(RechercheCategoryDTO category);

    /**
     * <p>Enregistre les changements apportés à la page administration de la reecherche avancée.</p>
     * <p>Met à jour les propriétés de la recherche avancée.</p>
     *
     * @param rechercheChampDTOS
     *         objet contenant la liste des propriétés éditées
     */
    void updateRechercheChamps(List<RechercheChampDTO> rechercheChampDTOS);

    /**
     * <p>Sauvegarde en base de données de la propriété en paramètre.</p>
     * <p>Créé une nouvelle propriété si celle-ci n'est pas trouvée.</p>
     * <p>Associe une catégorie à la propriété si précisée.</p>
     *
     * @param rechercheChampDTO
     *         la propriété à sauvegarder
     */
    void updateRechercheChamp(RechercheChampDTO rechercheChampDTO);

    void deleteCategory(Integer id);

    List<RechercheCategoryDTO> updateCategories(List<RechercheCategoryDTO> categories);

    RechercheCategoryDTO addCategory(String label);

    String exportConfig() throws IOException;

    void importConfig(byte[] file) throws IOException;

}
