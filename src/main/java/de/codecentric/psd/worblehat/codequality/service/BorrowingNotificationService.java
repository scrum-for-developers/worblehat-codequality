package de.codecentric.psd.worblehat.codequality.service;

import com.google.common.collect.ImmutableMap;
import de.codecentric.psd.worblehat.codequality.configuration.MailSettings;
import de.codecentric.psd.worblehat.codequality.configuration.SMTPSettings;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Borrowing;
import de.codecentric.psd.worblehat.codequality.persistence.repository.BorrowingRepository;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class BorrowingNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(BorrowingNotificationService.class);

    @Autowired
    private SMTPSettings smtp;

    @Autowired
    private MailSettings ml;

    @Autowired
    private BorrowingRepository rep;

    public void notifyBorrowersIfTheirBookBoorowingsLastLongerThanTheAllowedLimit() {
        // setup ml server
        Properties p = System.getProperties();
        p.setProperty("mail.smtp.host", smtp.getHost());
        p.setProperty("mail.smtp.port", smtp.getPort());

        // get the default Session object.
        Session session = Session.getDefaultInstance(p);

        List<Borrowing> bs = rep.findAllBorrowings();

        for (Borrowing b : bs) {
            long d = TimeUnit.DAYS.convert(new Date().getTime() - b.getBorrowDate().getTime(), TimeUnit.MILLISECONDS);
            if (LOG.isDebugEnabled()) LOG.debug("Check borrowing (age: {} days) (content: {}).", d, b);
            if (d >= 20) {
                if (d <= 28) {
                    // inform that borrowing period ends next week
                    try {
                        MimeMessage message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(ml.getFrom()));
                        message.addRecipient(Message.RecipientType.TO, new InternetAddress(b.getBorrowerEmailAddress()));
                        message.setSubject("Worblehat reminder: Your borrowing period for a book ends next week!");
                        message.setText(StrSubstitutor.replace(
                                "Hello,\n"
                                        + "\n"
                                        + "your borrowing period for the book '${bookTitle}' (ISBN: ${isbn}) ends next week.\n"
                                        + "Please return the book in the next ${daysLeft} days otherwise we have to claim a surcharge (see tariffs below).\n"
                                        + "\n"
                                        + "1 € for the first week after borrowing period expired\n"
                                        + "2 € for every further week\n"
                                        + "\n"
                                        + "Best regards,\n"
                                        + "your Worblehat team.",
                                ImmutableMap.of(
                                        "bookTitle", b.getBorrowedBook().getTitle(),
                                        "isbn", b.getBorrowedBook().getIsbn(),
                                        "daysLeft", 28 - d
                                )));
                        Transport.send(message);
                        LOG.info("Successfully sent message to recipient '{}'.", b.getBorrowerEmailAddress());
                    } catch (MessagingException e) {
                        LOG.warn(String.format("Failed to send message to recipient '%s'.", b.getBorrowerEmailAddress()), e);
                    }
                } else if ((d - 28) % 7 == 0) {
                    // inform that borrowing period has ended and that the borrower has to pay a surcharge
                    try {
                        double fee = 1.00 + d >= 36 ? Math.ceil((d - 35) / 7d) * 2 : 0;

                        MimeMessage m = new MimeMessage(session);
                        m.setFrom(new InternetAddress(ml.getFrom()));
                        m.addRecipient(Message.RecipientType.TO, new InternetAddress(b.getBorrowerEmailAddress()));
                        m.setSubject("Worblehat reminder: Your borrowing period for a book has ended!");
                        m.setText(StrSubstitutor.replace(
                                "Hello,\n"
                                        + "\n"
                                        + "your borrowing period for the book '${bookTitle}' (ISBN: ${isbn}) ended ${daysOverDueDate} days ago.\n"
                                        + "\n"
                                        + "For the delay we charge you a fee of ${fee} €.\n"
                                        + "\n"
                                        + "Please return the book as soon as possible and remember to bring enough money to pay your bill (we also accept credit cards).\n"
                                        + "\n"
                                        + "Our tariffs: \n"
                                        + "\n"
                                        + "1 € for the first week after borrowing period expired\n"
                                        + "2 € for every further week\n"
                                        + "\n"
                                        + "Best regards,\n"
                                        + "your Worblehat team.",
                                ImmutableMap.of(
                                        "bookTitle", b.getBorrowedBook().getTitle(),
                                        "isbn", b.getBorrowedBook().getIsbn(),
                                        "daysOverDueDate", String.valueOf(d - 28),
                                        "fee", String.format("%.2f", fee)
                                )
                        ));
                        Transport.send(m);
                        LOG.info("Successfully sent message to recipient '{}'.", b.getBorrowerEmailAddress());
                    } catch (MessagingException e) {
                        LOG.warn(String.format("Failed to send message to recipient '%s'.", b.getBorrowerEmailAddress()), e);
                    }
                } else {
                    LOG.info("");
                }
            }
        }
    }
}
