package mc.gouv.xaf.back.service.data.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.IOException;
import java.util.List;
import mc.gouv.xaf.back.data.dao.DemandesConfigRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.transformer.DemandesConfigTransformer;
import mc.gouv.xaf.back.exception.DemarcheException;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des demandes.
 *
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesConfigServiceImpl implements DemandesConfigService {

	@Autowired
	private DemandesConfigRepository demandesConfigRepository;

	@Autowired
	private MarqueursService marqueursService;

	@Autowired
	private DemandesConfigTransformer demandesConfigTransformer;

	@Override
	public List<String> getBuildIds() {
		return getConfigsBO().stream().map(DemandeConfigBO::getBuildId).toList();
	}

	@Override
	public List<DemandeConfigBO> getConfigsBO() {
		return demandesConfigRepository.findAllByOrderByBuildIdDesc();
	}

	@Override
	public String getLastBuildId() {
		DemandeConfigBO configBO = demandesConfigRepository.findFirstByOrderByBuildIdDesc();
		return configBO != null ? configBO.getBuildId() : null;
	}

	@Override
	public DemandeConfigBO getLastConfig() {
		return demandesConfigRepository.findFirstByOrderByBuildIdDesc();
	}

	@Override
	public JsonNode saveConfig(JsonNode config) {
		String buildId = config.get("buildId").asText();
		// si la config existe et que son contenu et != null, on ne la sauvegarde pas
		DemandeConfigBO configBO = demandesConfigRepository.findOneByBuildId(buildId);
		if (configBO == null || configBO.getContenu() == null) {
			String lastBuildId = getLastBuildId();
			configBO = demandesConfigRepository.save(demandesConfigTransformer.json2Bo(config));
			marqueursService.copyOrGenerateMarqueurs(lastBuildId, buildId, getModelPaths(config.get("modelPaths").get("rechercheAvancee")));
		}
		return demandesConfigTransformer.bo2Json(configBO);
	}

    @Override
    public List<String> getModelPathsRechercheAvancee() {
        return getModelPathsRechercheAvancee(getLastBuildId());
    }

	@Override
	public List<String> getModelPathsRechercheAvancee(String buildId) {
        return getModelPaths(buildId, "rechercheAvancee");
    }

    @Override
    public List<String> getModelPathsMarqueurs(String buildId) {
        return getModelPaths(buildId, "marqueurs");
    }

    private List<String> getModelPaths(String buildId, String node) {
        DemandeConfigBO configBO = demandesConfigRepository.findOneByBuildId(buildId);
        return getModelPaths(configBO.getContenu().get("modelPaths").get(node));
    }

    @Override
	public List<String> getModelPaths(JsonNode modelPaths) {
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.readerFor(new TypeReference<List<String>>() {});
		try {
			return reader.readValue(modelPaths);
		} catch (IOException e) {
			throw new DemarcheException("Erreur lors de la récupération des chemins", e);
		}
	}

}
