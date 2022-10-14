package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.service.PermisService;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class PermisServiceImpl implements PermisService {
    private final static Logger LOGGER = LoggerFactory.getLogger(PermisServiceImpl.class);

    @Autowired
    private FactureApiClient factureApiClient;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private MailService mailService;

    @Override
    public PermisDTO getPermis(String numPermis, int pkDemande) throws Exception {
        logStartMethod(LOGGER);

        PermisDTO permisDTO;
        try {
            permisDTO = factureApiClient.getPermis(numPermis).get();
        } catch (Exception e) {
            sendMailProblemeCir(pkDemande);
            throw e;
        }

        return permisDTO;
    }

    private void sendMailProblemeCir(int demandeId) {
        String subjectTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_CORPS";
        List<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name());
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, demandeId, 5, null);
    }
}
