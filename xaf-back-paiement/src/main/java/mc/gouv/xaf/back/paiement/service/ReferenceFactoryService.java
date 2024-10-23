package mc.gouv.xaf.back.paiement.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class ReferenceFactoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceFactoryService.class);

    public String createSimpleReference12Digits() {
        logStartMethod(LOGGER);
        return RandomStringUtils.random(12, 0, 0, true, true, null, new SecureRandom());
    }

    public String createSimpleReferenceDigitsNumeric(final int number) {
        logStartMethod(LOGGER);
        return RandomStringUtils.random(number, 0, 0, false, true, null, new SecureRandom());
    }

}
