package de.codecentric.psd.worblehat.codequality;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BorrowingNotificationScheduler {

    @Scheduled(fixedRateString = "${borrowing.notification.interval}")
    private void notifyBorrowers() {

    }
}
