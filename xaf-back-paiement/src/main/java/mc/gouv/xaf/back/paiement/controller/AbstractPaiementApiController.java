package mc.gouv.xaf.back.paiement.controller;

import mc.gouv.xaf.back.paiement.service.PaiementService;
import mc.gouv.xaf.shared.stc.dto.PaiementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import java.io.IOException;

public abstract class AbstractPaiementApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPaiementApiController.class);

    @Autowired
    private PaiementService paiementService;

    @GetMapping
    public PaiementDTO getPaiement(@RequestParam String demandesId,
                                   @RequestParam String langue,
                                   @RequestParam Integer usagerId) {

        return paiementService.create(demandesId, langue, usagerId);
    }

    @GetMapping(value = "{reference}/status/{status}")
    public void updatePaiement(@PathVariable String reference, @PathVariable String status) throws IOException, SAXException {
        paiementService.updateStatus(reference, status);
    }


}
