package mc.gouv.xaf.back.paiement.service;

import com.fasterxml.jackson.databind.JsonNode;
import mc.gouv.xaf.shared.paiement.tableaupaiement.CellsDTO;
import mc.gouv.xaf.shared.paiement.tableaupaiement.TableauDTO;

public interface TableauPaiementService {

    default TableauDTO getItemTableauPaiement(JsonNode item, Integer id) {
        return null;
    }
    default CellsDTO getCells() {
        return null;
    }
}
