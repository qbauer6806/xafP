package mc.gouv.af.back.mail;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.subethamail.wiser.Wiser;

import mc.gouv.af.back.AfBackServiceTestConfiguration;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes=AfBackServiceTestConfiguration.class)
public class MailServiceTest {
	private Wiser wiser; 
	
	 @Value("${smtp.server.port}")
	    private final Integer port = null;
	 
	@Autowired
	private MailService mailService;
	
	@Before
	public void setup() {
		wiser = new Wiser();
		wiser.setPort(port);
		wiser.start();
	}
	
//	@After
//	punlic void tearDown( throws s)
	
	
	
}
