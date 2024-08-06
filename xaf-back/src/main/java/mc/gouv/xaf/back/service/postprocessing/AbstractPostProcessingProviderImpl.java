package mc.gouv.xaf.back.service.postprocessing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.enums.SourceFiablesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractPostProcessingProviderImpl implements PostProcessingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostProcessingProviderImpl.class);

    @Override
    public DemandeDTO postprocess(DemandeDTO demande, JsonNode donneesExternes) {
        JsonNode contenu = demande.getContenu();
        if (donneesExternes != null) {
            ObjectMapper objectMapper = new ObjectMapper();

            List<SourceFiableDTO> sourceFiableDTOS = new ArrayList<>();
            Map<String, String> donneesExternesMap = objectMapper.convertValue(donneesExternes, Map.class);
            for (Entry<String, String> donneeExterne : donneesExternesMap.entrySet()) {
                String nouvelleValeur = null;
                SourceFiablesEnum sourceFiablesEnum = null;
                // "usager.donneesExternes.mconnect.givenName"
                String donneeExterneValue = donneeExterne.getValue();
                // [usager.donneesExternes,mconnect,givenName]
                String[] donneeExterneValueArray = donneeExterneValue.split("\\.");
                // "givenName"
                String donneeExterneValueField = donneeExterneValueArray[donneeExterneValueArray.length - 1];
                if (donneeExterneValue.contains("mconnect") && demande.getDonneesMConnect() != null) {
                    sourceFiablesEnum = SourceFiablesEnum.MCONNECT;
                    switch (donneeExterneValueField) {
                        case "familyName":
                            nouvelleValeur = demande.getDonneesMConnect().getFamilyName();
                            break;
                        case "birthDatetime":
                            nouvelleValeur = AfBackUtils.mConnectDateToString(demande.getDonneesMConnect().getBirthDatetime());
                            break;
                        case "birthName":
                            nouvelleValeur = demande.getDonneesMConnect().getBirthName();
                            break;
                        case "givenName":
                            nouvelleValeur = demande.getDonneesMConnect().getGivenName();
                            break;
                        case "authority":
                            nouvelleValeur = demande.getDonneesMConnect().getAuthority();
                            break;
                        case "birthPlaceCountry":
                            nouvelleValeur = demande.getDonneesMConnect().getBirthPlaceCountry();
                            break;
                        case "birthPlaceCity":
                            nouvelleValeur = demande.getDonneesMConnect().getBirthPlaceCity();
                            break;
                        case "birthPlace":
                            nouvelleValeur = demande.getDonneesMConnect().getBirthPlace();
                            break;
                        default:
                            LOGGER.info("donneeExterneValueField inconnue : {}", donneeExterneValueField);
                    }
                } else if (donneeExterneValue.contains("resid")) {
                    // todo
                }
                // Si on a des données à remplacer
                if (nouvelleValeur != null) {
                    // "contenu.donnee.demandeur.prenom"
                    String donneeExterneKey = donneeExterne.getKey();
                    AfBackUtils.setNodeValue(contenu, donneeExterneKey, nouvelleValeur);
                    sourceFiableDTOS.add(new SourceFiableDTO(donneeExterneKey, sourceFiablesEnum));
                }
            }
            demande.setDonneesCertifiees(sourceFiableDTOS.toArray(SourceFiableDTO[]::new));
        }

        demande.setContenu(contenu);
        return demande;
    }

}
