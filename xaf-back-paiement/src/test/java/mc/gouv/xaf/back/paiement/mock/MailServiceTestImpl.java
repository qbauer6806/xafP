package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.enums.MailAudienceEnum;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;

@Component
public class MailServiceTestImpl implements MailService {
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, MailAudienceEnum audienceMail) throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments) throws JsonProcessingException {

    }

    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model, Map<String, InputStream> attachments, MailAudienceEnum audienceMail) throws JsonProcessingException {

    }

    @Override
    public String[] getMailPreview(String bodyTemplateCode, String subjectTemplateCode, String langue, Map<String, Object> model) throws ParseErrorException, MethodInvocationException, ResourceNotFoundException, Exception {
        return new String[0];
    }

    @Override
    public String formatCommentaire(String commentaire) {
        return null;
    }

    @Override
    public void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, Set<String> mailingLists, Integer pkDemande,
                                String identifiantDemande,  int incident, Map<String, Object> modelAdd, Map<String, InputStream> attachments) {

    }

    @Override
    public Set<String> getMailingLists(String... mailingListProps) {
        return null;
    }


}
