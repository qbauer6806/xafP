package mc.gouv.xaf.back.stc.controller;

import mc.gouv.xaf.back.stc.service.PaiementService;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public abstract class AbstractStcApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStcApiController.class);

    @Autowired
    private PaiementService paiementService;

    @GetMapping
    public PaiementDTO getPaiement(@RequestParam Integer demandeId,
                                   @RequestParam String langue,
                                   @RequestParam Integer usagerId) {

        return paiementService.create(demandeId, langue, usagerId);
    }

    @GetMapping(value = "{reference}/status/{status}")
    public void getPaiement(@PathVariable String reference, @PathVariable String status) {
        paiementService.updateStatus(reference, status);
    }


}
