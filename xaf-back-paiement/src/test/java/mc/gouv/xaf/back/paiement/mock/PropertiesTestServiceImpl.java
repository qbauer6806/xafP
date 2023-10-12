package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.PropertiesTypeEnum;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PropertiesTestServiceImpl implements PropertiesService {
    @Override
    public List<PropertiesDTO> getProperties() {
        return null;
    }

    @Override
    public List<PropertiesDTO> getPropertiesByType(PropertiesTypeEnum type) {
        return null;
    }

    @Override
    public List<PropertiesDTO> getPropertiesByTypeList(List<PropertiesTypeEnum> types) {
        return null;
    }

    @Override
    public List<PropertiesDTO> getFrontProperties() {
        return null;
    }

    @Override
    public List<PropertiesDTO> getAdminsFonctionnelsProperties() {
        return null;
    }

    @Override
    public PropertiesDTO saveOrUpdateProperties(PropertiesDTO toSave) {
        return null;
    }

    @Override
    public void deleteProperties(Integer propertiesId) {

    }

    @Override
    public PropertiesDTO getProperty(String demarcheId, String key) {
        if ("PERMC".equals(demarcheId) && "XAF_TARIF_ECHANGE".equals(key))
            return new PropertiesDTO("amount", "80.00");
        return null;
    }

    @Override
    public String getPropertyPourRecap(String key, JsonNode pathNode, boolean recap) {
        return null;
    }

    @Override
    public PropertiesDTO updatePropertyValue(Integer pkProperties, String value) {
        return null;
    }
}
