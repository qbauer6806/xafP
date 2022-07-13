package mc.gouv.xaf.back.paiement.controller;

import mc.gouv.xaf.back.paiement.dto.PaiementDTO;
import mc.gouv.xaf.back.paiement.service.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractPaiementApiController {

    @Autowired
    private PaiementService paiementService;

    @GetMapping
    public PaiementDTO getPaiement(@RequestParam String demandesId,
                                   @RequestParam String langue,
                                   @RequestParam Integer usagerId,
                                   @RequestParam boolean iframe) {

        return paiementService.create(demandesId, langue, usagerId, iframe);
    }

    @GetMapping(value = "{reference}/status/{status}")
    public void updatePaiement(@PathVariable String reference, @PathVariable String status) {
        paiementService.updateStatus(reference, status);
    }


}
