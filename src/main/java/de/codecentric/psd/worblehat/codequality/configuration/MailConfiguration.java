package de.codecentric.psd.worblehat.codequality.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.mail.Session;
import java.util.Properties;

@Configuration
public class MailConfiguration {

    @Bean
    public Session mailSession(SMTPSettings smtpSettings) {
        // setup maill server
        Properties mailProperties = System.getProperties();
        mailProperties.setProperty("mail.smtp.host", smtpSettings.getHost());
        mailProperties.setProperty("mail.smtp.port", smtpSettings.getPort());

        // get the default Session object.
        return Session.getDefaultInstance(mailProperties);
    }
}
