package mc.gouv.xaf.back.service.data.impl;

import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.MarqueursRepository;
import mc.gouv.xaf.back.data.entity.MarqueurBO;
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
	public MarqueurDTO getMarqueur(String identifiant, String buildId) {
		MarqueurBO marqueurBO = marqueursRepository.findOneByIdentifiantAndBuildId(identifiant, buildId);
        // si on ne retrouve pas le marqueur alors on essaye de le créer automatiquement à partir de son identifiant
        if (marqueurBO == null) {
            MarqueurDTO newMarqueur = new MarqueurDTO();
            newMarqueur.setIdentifiant(identifiant);
            newMarqueur.setBuildId(buildId);
            // expression régulière pour trouver les majuscules et les remplacer par ".lettre minuscule"
            String chemin = "contenu." + identifiant.replaceAll("([a-z])([A-Z])", "$1.$2").toLowerCase();
            // vérifier si le chemin existe, alors on peut lui associer un chemin
            if (demandesConfigService.checkIfCheminExists(chemin, buildId)) {
                newMarqueur.setChemin(chemin);
            }
            // un marqueur sans chemin est quand même créé
            return saveOrUpdateMarqueur(newMarqueur);
        }
		return marqueursTransformer.bo2Dto(marqueurBO);
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
    public void copyMarqueurs(String lastBuildId, String buildId, List<String> modelPaths) {
		// on copie les marqueurs du précédent build id
		if (lastBuildId != null) {
			List<MarqueurDTO> marqueurDTOS = getMarqueurs(lastBuildId);
			for (MarqueurDTO marqueurDTO : marqueurDTOS) {
				marqueurDTO.setPkMarqueur(null);
				marqueurDTO.setBuildId(buildId);
				// vérifier si le chemin existe toujours dans le nouveau config
				if (marqueurDTO.getChemin() != null && !modelPaths.contains(marqueurDTO.getChemin())) {
					marqueurDTO.setChemin(null);
				}
			}
			marqueursRepository.saveAll(marqueursTransformer.dtos2Bos(marqueurDTOS));
		}
	}
}
