package mc.gouv.af.back.mail;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import mc.gouv.af.apiserver.AfApiController;
import mc.gouv.af.back.mail.MailService;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes=AfApiController.class)
public class MailServiceTest {
	
	@Autowired
	private MailService mailService;
	
}
