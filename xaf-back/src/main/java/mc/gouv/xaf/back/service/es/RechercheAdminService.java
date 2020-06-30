package mc.gouv.xaf.back.service.es;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.es.model.ConfigCategoriesDTO;
import mc.gouv.xaf.back.data.es.model.ConfigPropertiesDTO;
import mc.gouv.xaf.back.data.es.model.ConfigPropertyDTO;
import mc.gouv.xaf.back.data.es.model.EsCategory;
import mc.gouv.xaf.back.data.es.model.EsProperty;

public interface RechercheAdminService {

    Map<String, RechercheChampConfigBO> getChampsMap();

    List<EsProperty> getPropertiesWithLabels();

    List<EsCategory> getCategories();

    EsCategory updateCategory(EsCategory category);

    /**
     * <p>Enregistre les changements apportés à la page administration de la reecherche avancée.</p>
     * <p>Met à jour les propriétés de la recherche avancée.</p>
     * @param properties objet contenant la liste des propriétés éditées
     */
    void updateProperties(ConfigPropertiesDTO properties);

    /**
     * <p>Sauvegarde en base de données de la propriété en paramètre.</p>
     * <p>Créé une nouvelle propriété si celle-ci n'est pas trouvée.</p>
     * <p>Associe une catégorie à la propriété si précisée.</p>
     * @param property la propriété à sauvegarder
     */
    void updateProperty(ConfigPropertyDTO property);

    void deleteCategory(Integer id);

    List<EsCategory> updateCategories(ConfigCategoriesDTO categories);

    EsCategory addCategory(String label);

    String exportConfig() throws JsonGenerationException, JsonMappingException, IOException;

    void importConfig(byte[] file) throws JsonParseException, JsonMappingException, IOException;

}
