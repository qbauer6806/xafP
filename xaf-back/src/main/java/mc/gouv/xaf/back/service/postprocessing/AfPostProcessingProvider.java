package mc.gouv.xaf.back.service.postprocessing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DonneesMConnectDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.enums.SourceFiablesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AfPostProcessingProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfPostProcessingProvider.class);

    private final PostProcessingProvider postProcessingProvider;

    public DemandeDTO postprocess(DemandeDTO demande, JsonNode donneesExternes) {
        JsonNode contenu = demande.getContenu();
        if (donneesExternes != null) {
            ObjectMapper objectMapper = new ObjectMapper();

            List<SourceFiableDTO> sourceFiableDTOS = new ArrayList<>();
            Map<String, String> donneesExternesMap = objectMapper.convertValue(donneesExternes, new TypeReference<>() {

            });
            SourceFiablesEnum sourceFiablesEnum = this.getSourceFiablesEnum(demande.getDonneesMConnect());
            for (Entry<String, String> donneeExterne : donneesExternesMap.entrySet()) {
                String nouvelleValeur = this.getNouvelleValeur(donneeExterne, demande.getDonneesMConnect());
                // Si on a des données à remplacer
                if (nouvelleValeur != null) {
                    // "contenu.donnee.demandeur.prenom"
                    String donneeExterneKey = donneeExterne.getKey();
                    AfBackUtils.setNodeValue(contenu, donneeExterneKey, nouvelleValeur);
                    sourceFiableDTOS.add(new SourceFiableDTO(donneeExterneKey, sourceFiablesEnum));
                }
            }
            if (!sourceFiableDTOS.isEmpty()) {
                demande.setDonneesCertifiees(sourceFiableDTOS.toArray(SourceFiableDTO[]::new));
            }
        }

        demande.setContenu(contenu);

        // code spécifique TS si besoin
        demande = postProcessingProvider.postprocess(demande, donneesExternes);
        return demande;
    }

    private String getNouvelleValeur(Entry<String, String> donneeExterne, DonneesMConnectDTO donneesMConnectDTO) {
        String nouvelleValeur = null;
        // "usager.donneesExternes.mconnect.givenName"
        String donneeExterneValue = donneeExterne.getValue();
        // [usager.donneesExternes,mconnect,givenName]
        String[] donneeExterneValueArray = donneeExterneValue.split("\\.");
        // "givenName"
        String donneeExterneValueField = donneeExterneValueArray[donneeExterneValueArray.length - 1];
        if (donneeExterneValue.contains("mconnect") && donneesMConnectDTO != null) {
            switch (donneeExterneValueField) {
                case "familyName":
                    nouvelleValeur = donneesMConnectDTO.getFamilyName();
                    break;
                case "birthDatetime":
                    nouvelleValeur = AfBackUtils.mConnectDateToString(donneesMConnectDTO.getBirthDatetime());
                    break;
                case "birthName":
                    nouvelleValeur = donneesMConnectDTO.getBirthName();
                    break;
                case "givenName":
                    nouvelleValeur = donneesMConnectDTO.getGivenName();
                    break;
                case "authority":
                    nouvelleValeur = donneesMConnectDTO.getAuthority();
                    break;
                case "birthPlaceCountry":
                    nouvelleValeur = AfBackUtils.getAlpha2Code(donneesMConnectDTO.getBirthPlaceCountry());
                    break;
                case "birthPlaceCity":
                    nouvelleValeur = donneesMConnectDTO.getBirthPlaceCity();
                    break;
                case "birthPlace":
                    nouvelleValeur = donneesMConnectDTO.getBirthPlace();
                    break;
                default:
                    LOGGER.info("donneeExterneValueField inconnue : {}", donneeExterneValueField);
            }
        } else if (donneeExterneValue.contains("resid")) {
            // TODO
        }
        return nouvelleValeur;
    }

    private SourceFiablesEnum getSourceFiablesEnum(DonneesMConnectDTO donneesMConnectDTO) {
        if (donneesMConnectDTO == null) {
            return null;
        }
        try {
            return SourceFiablesEnum.valueOf(donneesMConnectDTO.getAuthority());
        } catch (Exception e) {
            //Au cas ou authority est null ou la valeur non présente dans l'énum
            return SourceFiablesEnum.MCONNECT;
        }
    }

}
