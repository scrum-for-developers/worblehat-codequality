package de.codecentric.psd.worblehat.codequality.service;

import de.codecentric.psd.worblehat.codequality.configuration.MailSettings;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Borrowing;
import de.codecentric.psd.worblehat.codequality.persistence.repository.BorrowingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static de.codecentric.psd.worblehat.codequality.service.Mail.aMail;

/**
 * Service for sending notifications to borrowers that their borrowing time is about to exceed or already has exceeded.
 */
@Component
public class BorrowingNotificationService {

    private static final Logger LOG = LoggerFactory.getLogger(BorrowingNotificationService.class);

    public static final int MAX_BORROW_DAYS = 28;
    public static final double FEE_FOR_FIRST_WEEK = 1.00;
    public static final double FEE_FOR_SECOND_AND_FURTHER_WEEKS = 2.00;

    private MailSettings mailSettings;
    private MailingService mailingService;
    private BorrowingRepository borrowingRepository;

    @Autowired
    public BorrowingNotificationService(MailSettings mailSettings, MailingService mailingService, BorrowingRepository borrowingRepository) {
        this.mailSettings = mailSettings;
        this.mailingService = mailingService;
        this.borrowingRepository = borrowingRepository;
    }

    public void notifyBorrowers() {
        List<Borrowing> borrowings = borrowingRepository.findAllBorrowings();

        for (Borrowing borrowing : borrowings) {
            long borrowedDays = TimeUnit.DAYS.convert(new Date().getTime() - borrowing.getBorrowDate().getTime(), TimeUnit.MILLISECONDS);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Check borrowing (age: {} days) (content: {}).", borrowedDays, borrowing);
            }
            if (borrowedDays >= 20) {
                if (borrowedDays <= MAX_BORROW_DAYS) {
                    notifyAboutUpcomingBorrowingEnd(borrowing, borrowedDays);
                } else {
                    notifyAboutExceededBorrowingDuration(borrowing, borrowedDays);
                }
            }
        }
    }

    /**
     * Inform borrower that borrowing period ends next week
     */
    private void notifyAboutUpcomingBorrowingEnd(Borrowing borrowing, long borrowedDays) {
        mailingService.sendMail(
                aMail()
                        .from(mailSettings.getFrom())
                        .to(borrowing.getBorrowerEmailAddress())
                        .withSubject("Worblehat reminder: Your borrowing period for a book ends soon!")
                        .withBody(
                                "Hello,",
                                "",
                                "your borrowing period for the book '${bookTitle}' (ISBN: ${isbn}) ends next week.",
                                "Please return the book in the next ${daysLeft} days otherwise we have to claim a surcharge (see tariffs below).",
                                "",
                                "1 € for the first week after borrowing period expired",
                                "2 € for every further week",
                                "",
                                "Best regards,",
                                "your Worblehat team."
                        )
                        .withArgument("bookTitle", borrowing.getBorrowedBook().getTitle())
                        .withArgument("isbn", borrowing.getBorrowedBook().getIsbn())
                        .withArgument("daysLeft", String.valueOf(MAX_BORROW_DAYS - borrowedDays))
        );
    }

    /**
     * Inform borrower that the borrowing period has ended and that the borrower has to pay a surcharge.
     */
    private void notifyAboutExceededBorrowingDuration(Borrowing borrowing, long borrowedDays) {
        double fee = calculateFee(borrowedDays);

        mailingService.sendMail(
                aMail()
                        .from(mailSettings.getFrom())
                        .to(borrowing.getBorrowerEmailAddress())
                        .withSubject("Worblehat reminder: Your borrowing period for a book has ended!")
                        .withBody(
                                "Hello,",
                                "",
                                "your borrowing period for the book '${bookTitle}' (ISBN: ${isbn}) ended ${daysOverDueDate} days ago.",
                                "",
                                "For the delay we charge you a fee of ${fee} €.",
                                "",
                                "Please return the book as soon as possible and remember to bring enough money to pay your bill (we also accept credit cards).",
                                "",
                                "Our tariffs:",
                                "",
                                "1 € for the first week after borrowing period expired",
                                "2 € for every further week",
                                "",
                                "Best regards,",
                                "your Worblehat team."
                        )
                        .withArgument("bookTitle", borrowing.getBorrowedBook().getTitle())
                        .withArgument("isbn", borrowing.getBorrowedBook().getIsbn())
                        .withArgument("daysOverDueDate", String.valueOf(borrowedDays - MAX_BORROW_DAYS))
                        .withArgument("fee", String.format("%.2f", fee))
        );
    }

    private double calculateFee(long borrowedDays) {
        double fee = 0;
        if (borrowedDays > MAX_BORROW_DAYS) {
            fee += FEE_FOR_FIRST_WEEK;
        }
        if (borrowedDays >= 36) {
            fee += Math.ceil((borrowedDays - 35) / 7d) * FEE_FOR_SECOND_AND_FURTHER_WEEKS;
        }
        return fee;
    }
}
