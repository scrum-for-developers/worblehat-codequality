package de.codecentric.psd.worblehat.codequality.service;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import de.codecentric.psd.worblehat.codequality.configuration.SMTPSettings;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Session;
import java.util.Properties;

import static de.codecentric.psd.worblehat.codequality.service.Mail.aMail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class MailingServiceTest {

    private SimpleSmtpServer smtpServer;
    private MailingService mailingService;

    @Before
    public void setUp() throws Exception {
        SMTPSettings smtpSettings = new SMTPSettings();
        smtpSettings.setHost("127.0.0.1");
        smtpSettings.setPort("10025");
        smtpServer = SimpleSmtpServer.start(10025);

        Properties mailProperties = System.getProperties();
        mailProperties.setProperty("mail.smtp.host", smtpSettings.getHost());
        mailProperties.setProperty("mail.smtp.port", smtpSettings.getPort());
        mailingService = new MailingService(Session.getDefaultInstance(mailProperties));
    }

    @After
    public void tearDown() {
        smtpServer.stop();
    }

    @Test
    public void shouldSendMail() {
        mailingService.sendMail(
                aMail()
                        .from("sender@test.xyz")
                        .to("recipient@test.xyz")
                        .withSubject("test subject")
                        .withBody(
                                "body line 1 ${argument1}",
                                "body line 2 ${argument2}"
                        )
                        .withArgument("argument1", "replacement1")
                        .withArgument("argument2", "replacement2")
        );

        assertThat(smtpServer.getReceivedEmails().size(), is(1));
        SmtpMessage message = smtpServer.getReceivedEmails().get(0);
        assertThat(message.getHeaderValue("From"), is(equalTo("sender@test.xyz")));
        assertThat(message.getHeaderValue("To"), is(equalTo("recipient@test.xyz")));
        assertThat(message.getHeaderValue("Subject"), is(equalTo("test subject")));
        assertThat(message.getBody(), is(equalTo(
                "body line 1 replacement1body line 2 replacement2"
        )));
    }
}