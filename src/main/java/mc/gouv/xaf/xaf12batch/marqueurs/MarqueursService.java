package mc.gouv.xaf.xaf12batch.marqueurs;

import java.util.ArrayList;
import java.util.List;

import mc.gouv.xaf.xaf12batch.demandesconfig.DemandesConfigService;
import mc.gouv.xaf.xaf12batch.dto.DemandeConfigBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MarqueursService {

    @Autowired
    private MarqueursRepository marqueursRepository;

    @Autowired
    private MarqueursTransformer marqueursTransformer;

    @Autowired
    private DemandesConfigService demandesConfigService;

    public void resetMarqueurs() {
        marqueursRepository.deleteAll();
        List<DemandeConfigBO> configs = demandesConfigService.getConfigsBO();
        for (DemandeConfigBO config : configs) {
            List<MarqueurDTO> marqueurDTOS = new ArrayList<>();
            setMarqueursFromModelPaths(
                    demandesConfigService.getModelPaths(config.getContenu().get("modelPaths").get("rechercheAvancee")),
                    marqueurDTOS, config.getBuildId());
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
            result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }

        return result.toString();
    }


}
