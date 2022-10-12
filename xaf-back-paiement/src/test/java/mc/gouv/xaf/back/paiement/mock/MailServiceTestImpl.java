package mc.gouv.xaf.back.paiement.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import mc.gouv.xaf.back.service.itg.mail.EmailInfoDTO;
import mc.gouv.xaf.back.service.itg.mail.MailService;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class MailServiceTestImpl implements MailService {
    @Override
    public void sendMail(EmailInfoDTO emailInfo, Map<String, Object> model) throws JsonProcessingException {

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
    public void sendMailSupport(String subjectTemplateCode, String bodyTemplateCode, List<String> mailingLists, int demandeId, int incident, Map<String, Object> modelAdd) {

    }

    @Override
    public List<String> getMailingLists(String... mailingListProps) {
        return null;
    }


}
