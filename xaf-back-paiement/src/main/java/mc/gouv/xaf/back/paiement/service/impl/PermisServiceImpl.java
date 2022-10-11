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

    private SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");

    @Autowired
    private FactureApiClient factureApiClient;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private MailService mailService;

    @Override
    public PermisDTO getPermis(String numPermis, int pkDemande) throws Exception {
        logStartMethod(LOGGER);

        PermisDTO permisDTO;
        try {
            // TODO To remove after testing
            PropertiesDTO errorProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), "TEMP_FAIL_CIR_TABLE_PERMIS");
            if (errorProp != null && "true".equals(errorProp.getValue()) ) {
                throw new Exception();
            }

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
        List<String> list = getMailingLists(MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE.name(),
                MailSupportEnum.XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR.name());
        sendMail(subjectTemplateCode, bodyTemplateCode, demandeId, 5, list);
    }

    private void sendMail(String subjectTemplateCode, String bodyTemplateCode, int demandeId, int incident, List<String> mailingLists) {
        Date date = new Date(System.currentTimeMillis());
        String dateTimeString = simpleDateTimeFormat.format(date);

        EmailInfoDTO emailInfo = new EmailInfoDTO();
        emailInfo.setLangue("fr");
        emailInfo.setBodyTemplateCode(bodyTemplateCode);
        emailInfo.setSubjectTemplateCode(subjectTemplateCode);
        emailInfo.setFrom(afBackUtils.getDemarcheInfos().getEmailFrom(), afBackUtils.getDemarcheInfos().getEmailFromNom());
        emailInfo.setReplyto(afBackUtils.getDemarcheInfos().getEmailReplyto(), afBackUtils.getDemarcheInfos().getEmailReplytoNom());
        emailInfo.addParam(AfBackUtils.MAIL_METADATA_DEMANDEID, demandeId + "");

        for(String mailingList : mailingLists) {
            String[] adresses = mailingList.trim().split(",");
            for (String adresseMail : adresses) {
                emailInfo.addTo(adresseMail, "Support Technique");
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("incident", incident);
        model.put("dateTimeString", dateTimeString);
        model.put("Pkdemandes", demandeId);
        try {
            mailService.sendMail(emailInfo, model);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'envoi de l'email", e);
        }
    }

    private List<String> getMailingLists(String... mailingListProps) {
        List<String> list = new ArrayList<>();
        for(String mailProp : mailingListProps) {
            PropertiesDTO mailProperty = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), mailProp);
            if (mailProperty != null && StringUtils.isNotBlank(mailProperty.getValue())) {
                list.add(mailProperty.getValue());
            }
        }
        return list;
    }
}
