package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.back.service.itg.mail.dto.EmailInfoDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component
public class MailServiceTestImpl implements MailService {

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, MailAudienceEnum audienceMail)
            throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments)
            throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments,
            MailAudienceEnum audienceMail) throws JsonProcessingException {

    }

    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue,
            Map<String, Object> model) throws IOException {
        return new String[0];
    }

    @Override
    public void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, Set<String> mailingLists,
            Integer pkDemande, String identifiantDemande, int incident, Map<String, Object> modelAdd,
            Map<String, InputStream> attachments) {

    }

    @Override
    public Set<String> getMailingLists(String... mailingListProps) {
        return null;
    }

    @Override
    public String getEmailBodyTemplate(Expression bodyTemplateCode, Expression emailTemplateCode,
            DelegateExecution execution) {
        return "";
    }

    @Override
    public String getEmailSubjectTemplate(Expression subjectTemplateCode, Expression emailTemplateCode,
            DelegateExecution execution) {
        return "";
    }

}
