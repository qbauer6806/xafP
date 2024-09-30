package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.BadRequestException;
import mc.gouv.xaf.back.data.dao.MarqueursRepository;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
import mc.gouv.xaf.back.data.entity.RechercheCatConfigBO;
import mc.gouv.xaf.back.data.entity.RechercheChampConfigBO;
import mc.gouv.xaf.back.data.model.ExportImportCategoryDTO;
import mc.gouv.xaf.back.data.model.ExportImportConfigDTO;
import mc.gouv.xaf.back.data.model.ExportImportConfigPropertyDTO;
import mc.gouv.xaf.back.data.transformer.MarqueursTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.MarqueursService;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(rollbackFor = Exception.class)
public class MarqueursServiceImpl implements MarqueursService {

	@Autowired
	private MarqueursRepository marqueursRepository;

	@Autowired
	private MarqueursTransformer marqueursTransformer;

    @Autowired
    private DemandesConfigService demandesConfigService;

    @Override
	public List<MarqueurDTO> getMarqueurs(String buildId) {
		List<MarqueurBO> marqueurBOS = marqueursRepository.findAllByBuildId(buildId);
		return marqueursTransformer.bos2Dtos(marqueurBOS);
	}

	@Override
	public MarqueurDTO saveOrUpdateMarqueur(MarqueurDTO marqueurDTO) {
		// Création
		if (marqueurDTO.getPkMarqueur() == null) {
			MarqueurBO bo = marqueursTransformer.dto2Bo(marqueurDTO);
			bo = marqueursRepository.save(bo);
			return marqueursTransformer.bo2Dto(bo);
		}
		// Mise à jour
		else {
			Optional<MarqueurBO> marqueurBOOpt = marqueursRepository.findById(marqueurDTO.getPkMarqueur());
			if (marqueurBOOpt.isEmpty()) {
				throw new DemarchesServiceException("Le marqueur spécifié est introuvable", HttpStatus.NOT_FOUND);
			}

			MarqueurBO marqueurBO = marqueurBOOpt.get();
			marqueurBO.setDescription(marqueurDTO.getDescription());
			marqueurBO.setIdentifiant(marqueurDTO.getIdentifiant());
			marqueurBO.setChemin(marqueurDTO.getChemin());
			marqueurBO.setBuildId(marqueurDTO.getBuildId());
			marqueurBO = marqueursRepository.save(marqueurBO);

			return marqueursTransformer.bo2Dto(marqueurBO);

		}
	}

	@Override
	public void deleteMarqueur(Integer pkMarqueur) {
		Optional<MarqueurBO> marqueurBO = marqueursRepository.findById(pkMarqueur);
		if (marqueurBO.isEmpty()) {
			throw new DemarchesServiceException("Le marqueur spécifié est introuvable", HttpStatus.NOT_FOUND);
		}
		marqueursRepository.delete(marqueurBO.get());
	}

    @Override
    public void copyOrGenerateMarqueurs(String lastBuildId, String buildId, List<String> modelPaths) {
		if (lastBuildId != null) {
			List<MarqueurDTO> marqueurDTOS = getMarqueurs(lastBuildId);
            // on copie les marqueurs du précédent build id s'il y en a
            if(!marqueurDTOS.isEmpty()) {
                for (MarqueurDTO marqueurDTO : marqueurDTOS) {
                    marqueurDTO.setPkMarqueur(null);
                    marqueurDTO.setBuildId(buildId);
                    // vérifier si le chemin existe toujours dans le nouveau config
                    if (marqueurDTO.getChemin() != null && !modelPaths.contains(marqueurDTO.getChemin())) {
                        marqueurDTO.setChemin(null);
                    }
                }
            }
            // on génère tous les autres
            setMarqueursFromModelPaths(modelPaths, marqueurDTOS, buildId);

			marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
		}
	}

    @Override
    public void resetMarqueurs() {
        marqueursRepository.deleteAll();
        List<DemandeConfigBO> configs = demandesConfigService.getConfigsBO();
        for (DemandeConfigBO config : configs) {
            List<MarqueurDTO> marqueurDTOS = new ArrayList<>();
            setMarqueursFromModelPaths(demandesConfigService.getModelPaths(config.getContenu().get("modelPaths").get("rechercheAvancee")), marqueurDTOS, config.getBuildId());
            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
        }
    }

    private void setMarqueursFromModelPaths(List<String> modelPaths, List<MarqueurDTO> marqueurDTOS, String buildId) {
        for (String modelPath : modelPaths) {
            String id = pathToCamelCase(modelPath);
            // si le marqueur est déjà présent (du précédent buildId par exemple), on ne génère pas le marqueur
            if (marqueurDTOS.stream().noneMatch(marqueurDTO -> id.equals(marqueurDTO.getIdentifiant()))) {
                MarqueurDTO marqueur = new MarqueurDTO();
                marqueur.setChemin(modelPath);
                marqueur.setIdentifiant(id);
                marqueur.setBuildId(buildId);
                marqueurDTOS.add(marqueur);
            }
        }
    }

    private String pathToCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Diviser la chaîne en segments
        String[] parts = input.split("\\.");

        // Si la chaîne contient moins de deux parties, rien à transformer
        if (parts.length < 2) {
            return input;
        }

        StringBuilder result = new StringBuilder(parts[1]);

        for (int i = 2; i < parts.length; i++) {
            String part = parts[i];
            result.append(Character.toUpperCase(part.charAt(0)))
                    .append(part.substring(1));
        }

        return result.toString();
    }

    @Override
    public String exportConfig() throws IOException {
        Iterable<MarqueurBO> marqueurs = marqueursRepository.findAll();
        List<MarqueurDTO> list = new ArrayList<>();
        for (MarqueurBO marqueur : marqueurs) {
            list.add(marqueursTransformer.bo2Dto(marqueur));
        }

        ObjectMapper mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);
    }

    @Override
    public void importConfig(byte[] file) throws IOException {

        ObjectMapper mapper = new ObjectMapper();
        List<MarqueurDTO> marqueurList = mapper.readValue(file, new TypeReference<>() {});
        if (marqueurList != null) {
            marqueursRepository.deleteAll();
            marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurList));
        }


    }

}
