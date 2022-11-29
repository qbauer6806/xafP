package mc.gouv.xaf.back.paiement.service.impl;

import mc.gouv.xaf.back.paiement.dto.itg.cir.PermisDTO;
import mc.gouv.xaf.back.paiement.service.PermisService;
import mc.gouv.xaf.back.paiement.service.itg.FactureApiClient;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.enums.MailSupportEnum;
import org.apache.http.client.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static mc.gouv.xaf.back.paiement.LoggerMethodeUtils.logStartMethod;

@Service
public class PermisServiceImpl implements PermisService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PermisServiceImpl.class);

    @Autowired
    private FactureApiClient factureApiClient;

    @Autowired
    private MailService mailService;

    @Override
    public PermisDTO getPermis(String numPermis, int pkDemande, String identifiantDemande) throws HttpResponseException {
        logStartMethod(LOGGER);

        PermisDTO permisDTO;
        try {
            permisDTO = factureApiClient.getPermis(numPermis);
        } catch (HttpResponseException e) {
            sendMailProblemeCir(pkDemande, identifiantDemande, e.getMessage());
            throw e;
        }

        return permisDTO;
    }

    private void sendMailProblemeCir(int pkDemande, String identifiant, String reponse) {
        String subjectTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_OBJET";
        String bodyTemplateCode = "MAIL_ECHEC_CIR_TABLE_PERMIS_CORPS";
        Set<String> list = mailService.getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name());
        Map<String, Object> model = new HashMap<>();
        model.put("reponseApi", reponse);
        mailService.sendMailSupport(subjectTemplateCode, bodyTemplateCode, list, pkDemande, identifiant, 5, model, null);
    }
}
