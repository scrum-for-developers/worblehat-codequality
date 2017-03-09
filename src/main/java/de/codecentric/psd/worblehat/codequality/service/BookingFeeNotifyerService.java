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
import org.springframework.scheduling.annotation.Scheduled;
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
public class BookingFeeNotifyerService {

    private static final Logger LOG = LoggerFactory.getLogger(BookingFeeNotifyerService.class);

    @Autowired
    private SMTPSettings smtpSettings;

    @Autowired
    private MailSettings mailSettings;

    @Autowired
    private BorrowingRepository borrowingRepository;

    /**
     * Sonar (https://www.sonarqube.org):
     * <p>
     * $ ~/../sonar/bin/macosx-universal-64/sonar.sh start
     * -> http://localhost:9000/
     *
     * Run sonar maven goal:
     * $ mvn -Psonar sonar:sonar
     *
     * <p>
     * MailCatcher (https://mailcatcher.me):
     * <p>
     * $ mailcatcher -fv
     * -> http://localhost:1080/
     * <p>
     * <p>
     * - Tip: Zoom/use presentation mode when doing refactoring: methods get automatically shorter
     * - Code-Formatting (Shortcut in Idea: Cmd|Ctrl+Alt+L)
     * - Complexity
     * - Javadocs
     * <p>
     * Acceptance criteria:
     * * After three weeks (21 days) --> reminder: one week left to return book
     * * After four weeks (28 days) --> 0 €
     * * In the fith week (29 - 35 days) --> 1€
     * * Every week after that (>=36 days) --> 2€
     * <p>
     * <p>
     * Negativbeispiel in JavaX/Mail: {@link InternetAddress#parse(String s, boolean strict, boolean parseHdr)}, Zeile 692
     */


    @Scheduled(fixedRateString = "@borrowing.notification.interval@")
    private void sendMail() {
        // setup mail server
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", smtpSettings.getHost());
        properties.setProperty("mail.smtp.port", smtpSettings.getPort());

        // get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        List<Borrowing> borrowings = borrowingRepository.findAllBorrowings();

        for (Borrowing borrowing : borrowings) {
            // calculate fee

            long borrowedDays = calculateBorrowedDays(borrowing);
            if (borrowedDays < 20) {
                // ignore borrower
                continue;
            }
            if (borrowedDays <= 28) {
                // inform that borrowing period ends next week
                try {
                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailSettings.getFrom()));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(borrowing.getBorrowerEmailAddress()));
                    message.setSubject("Worblehat reminder: Your borrowing period for a book ends next week!");
                    message.setText(StrSubstitutor.replace(
                            "Hello,\n"
                                    + "\n"
                                    + "your borrowing period for the book '${bookTitle}' (ISBN: ${isbn}) ends next week.\n"
                                    + "Please return the book in the next 7 days otherwise we have to claim a surcharge (see tariffs below).\n"
                                    + "\n"
                                    + "1 € for the first week after borrowing period expired\n"
                                    + "2 € for every further week\n"
                                    + "\n"
                                    + "Best regards,\n"
                                    + "your Worblehat team.",
                            ImmutableMap.of(
                                    "bookTitle", borrowing.getBorrowedBook().getTitle(),
                                    "isbn", borrowing.getBorrowedBook().getIsbn()
                            )));
                    Transport.send(message);
                    LOG.info("Successfully sent message to recipient '{}'.", borrowing.getBorrowerEmailAddress());
                } catch (MessagingException e) {
                    LOG.warn(String.format("Failed to send message to recipient '%s'.", borrowing.getBorrowerEmailAddress()), e);
                }
            } else {
                // inform that borrowing period has ended and that the borrower has to pay a surcharge
                try {
                    double fee = 1.00;
                    if (borrowedDays >= 36) {
                        fee += Math.ceil((borrowedDays - 35) / 7d) * 2;
                    }

                    MimeMessage message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(mailSettings.getFrom()));
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(borrowing.getBorrowerEmailAddress()));
                    message.setSubject("Worblehat reminder: Your borrowing period for a book has ended!");
                    message.setText(StrSubstitutor.replace(
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
                                    "bookTitle", borrowing.getBorrowedBook().getTitle(),
                                    "isbn", borrowing.getBorrowedBook().getIsbn(),
                                    "daysOverDueDate", String.valueOf(borrowedDays - 28),
                                    "fee", String.format("%.2f", fee)
                            )
                    ));
                    Transport.send(message);
                    LOG.info("Successfully sent message to recipient '{}'.", borrowing.getBorrowerEmailAddress());
                } catch (MessagingException e) {
                    LOG.warn(String.format("Failed to send message to recipient '%s'.", borrowing.getBorrowerEmailAddress()), e);
                }
            }
        }
    }

    private long calculateBorrowedDays(Borrowing borrowing) {
        long now = new Date().getTime();
        long borrowTime = borrowing.getBorrowDate().getTime();
        long borrowdDays = TimeUnit.DAYS.convert(now - borrowTime, TimeUnit.MILLISECONDS);
        return borrowdDays;
    }
}
