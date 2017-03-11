package de.codecentric.psd.worblehat.codequality.service;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Service for sending emails.
 */
@Service
public class MailingService {

    private static final Logger LOG = LoggerFactory.getLogger(MailingService.class);

    private Session session;

    public MailingService(Session session) {
        this.session = session;
    }

    /**
     * Sends an email.
     *
     * @param mail the mail that is send.
     */
    public void sendMail(Mail mail) {
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mail.getFrom()));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(mail.getTo()));
            message.setSubject(StrSubstitutor.replace(mail.getSubject(), mail.getArguments()));
            message.setText(StrSubstitutor.replace(mail.getBody(), mail.getArguments()));
            Transport.send(message);
            LOG.info("Successfully sent message to recipient '{}'.", mail.getTo());
        } catch (MessagingException e) {
            LOG.warn(String.format("Failed to send message to recipient '%s'.", mail.getTo()), e);
        }
    }
}
