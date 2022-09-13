package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.service.PermisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class PermisServiceImpl implements PermisService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PermisServiceImpl.class);

    @Autowired
    private FactureApiClient factureApiClient;

    @Override
    public PermisDTO getPermis(String numPermis) throws Exception {
        logStartMethod(LOGGER);
        return factureApiClient.getPermis(numPermis).get();
    }
}
