package mc.gouv.xaf.back.paiement.service;


import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class ReferenceFactoryService {
    private static Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);
    public String createSimpleReference12Digits() {
        logStartMethod(LOGGER);
        final int SHORT_ID_LENGTH = 12;
        return RandomStringUtils.randomAlphanumeric(SHORT_ID_LENGTH);
    }

    public String createSimpleReferenceDigitsNumeric(int number) {
        logStartMethod(LOGGER);
        final int SHORT_ID_LENGTH = number;
        return RandomStringUtils.randomNumeric(SHORT_ID_LENGTH);
    }

}
