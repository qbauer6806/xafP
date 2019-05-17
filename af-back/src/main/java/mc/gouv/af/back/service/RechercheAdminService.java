package mc.gouv.af.back.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import mc.gouv.af.back.data.es.model.ConfigCategoriesDTO;
import mc.gouv.af.back.data.es.model.ConfigPropertiesDTO;
import mc.gouv.af.back.data.es.model.ConfigPropertyDTO;
import mc.gouv.af.back.data.es.model.EsCategory;
import mc.gouv.af.back.data.es.model.EsProperty;
import mc.gouv.af.data.entity.RechercheChampConfigBo;

public interface RechercheAdminService {

    Map<String, RechercheChampConfigBo> getChampsMap();

    List<EsProperty> getPropertiesWithLabels();

    List<EsCategory> getCategories();

    EsCategory updateCategory(EsCategory category);

    void updateProperties(ConfigPropertiesDTO properties);

    void updateProperty(ConfigPropertyDTO property);

    void deleteCategory(Integer id);

    List<EsCategory> updateCategories(ConfigCategoriesDTO categories);

    EsCategory addCategory(String label);

    String exportConfig() throws JsonGenerationException, JsonMappingException, IOException;

    void importConfig(byte[] file) throws JsonParseException, JsonMappingException, IOException;

}
