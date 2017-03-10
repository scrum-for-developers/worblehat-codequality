package de.codecentric.psd.worblehat.codequality;

import de.codecentric.psd.worblehat.codequality.service.BorrowingNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BorrowingNotificationScheduler {

    @Autowired
    private BorrowingNotificationService borrowingNotificationService;

    @Scheduled(fixedRateString = "${borrowing.notification.interval}")
    private void notifyBorrowers() {
        borrowingNotificationService.notify_borrowers_via_email_if_their_book_boorowings_last_longer_than_the_allowed_limit();
    }
}
