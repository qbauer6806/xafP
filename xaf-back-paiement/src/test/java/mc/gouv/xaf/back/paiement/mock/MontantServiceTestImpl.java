package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.paiement.service.MontantService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Iterator;

@Component
public class MontantServiceTestImpl implements MontantService {
    @Autowired
    private DemandesService demandesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public double getMontant(Integer demandeId) {
        DemandeDTO demandeDto = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                demandeId);
        return getMontant(demandeDto);
    }

    public double getMontant(DemandeDTO demandeDto) {
        return getPaiements(demandeDto).values().stream().reduce(0.0, Double::sum);
    }

    public double getMontant(HashMap<String, Double> objetMontants) {
        return objetMontants.values().stream().reduce(0.0, Double::sum);
    }

    public HashMap<String, Double> getPaiements(DemandeDTO demandeDto) {
        JsonNode contenuDemande = demandeDto.getContenu();

        HashMap<String, Double> objetMontants = new HashMap<>();
        Iterator<JsonNode> paiements = contenuDemande.get("paiement").get("tableau").elements();
        while (paiements.hasNext()) {
            JsonNode paiement = paiements.next();
            if (!paiement.isNull()) {
                objetMontants.put(paiement.get("objet").asText(), Double.parseDouble(paiement.get("montant").asText()));
            }

        }
        return objetMontants;
    }
}
