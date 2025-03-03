package mc.gouv.xaf.back.paiement.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.back.paiement.service.TableauPaiementService;
import mc.gouv.xaf.back.service.data.BrouillonsService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.shared.RequestConstant;
import mc.gouv.xaf.shared.dto.BrouillonDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class PaiementServiceImpl implements PaiementService {

    @Autowired
    private TableauPaiementService tableauPaiementService;

    @Autowired
    private BrouillonsService brouillonsService;

    @Autowired
    private DemandesService demandesService;

    @Override
    public List<TableauDTO> getTableauPaiement(String ids, String objectType, Integer usagerId) {
        List<TableauDTO> result = new ArrayList<>();
        List<String> idsList = Arrays.asList(ids.replace("[", "").replace("]", "").split(","));
        if (objectType.equals(RequestConstant.BROUILLONS_PATH)) {
            for (String currentId : idsList) {
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                BrouillonDTO brouillon = brouillonsService.getBrouillon(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = brouillon.getContenu();
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, brouillon.getPkBrouillons());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        } else if (objectType.equals(RequestConstant.DEMANDES_PATH)) {
            for (String currentId : idsList) {
                // On va chercher l'objet dans l'implémentation de TableauPaiementService propre à chaque TS
                DemandeDTO demande = demandesService.getDemande(Integer.valueOf(currentId), usagerId);
                JsonNode contenu = demande.getContenu();
                TableauDTO itemTableauPaiement = tableauPaiementService.getItemTableauPaiement(contenu, demande.getPkDemandes());
                if (null != itemTableauPaiement) {
                    result.add(itemTableauPaiement);
                }
            }
        }
        return result;
    }
}
