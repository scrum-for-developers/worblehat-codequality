package de.codecentric.psd.worblehat.codequality.service;

import com.dumbster.smtp.SimpleSmtpServer;
import de.codecentric.psd.worblehat.codequality.configuration.MailSettings;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Book;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Borrowing;
import de.codecentric.psd.worblehat.codequality.persistence.repository.BorrowingRepository;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class BorrowingNotificationServiceTest {

    private SimpleSmtpServer smtpServer;
    private BorrowingNotificationService borrowingNotificationService;
    private MailingService mailingService;
    private BorrowingRepository borrowingRepository;

    @Before
    public void before() throws Exception {
        MailSettings mailSettings = new MailSettings();
        mailSettings.setFrom("from.address@test.abc");

        borrowingRepository = Mockito.mock(BorrowingRepository.class);
        mailingService = Mockito.mock(MailingService.class);
        borrowingNotificationService = new BorrowingNotificationService(mailSettings, mailingService, borrowingRepository);
    }


    @Test
    public void shouldNotSendAMessageIfNoBooksAreBorrowed() throws Exception {
        when(borrowingRepository.findAllBorrowings()).thenReturn(Collections.emptyList());
        borrowingNotificationService.notifyBorrowers();
        verifyZeroInteractions(mailingService);
    }

    @Test
    public void shouldSendReminderIfBookIs2DaysBeforeDueDate() {
        Book book = new Book("title", "author", "edition", "isbn", 1234);
        when(borrowingRepository.findAllBorrowings())
                .thenReturn(Arrays.asList(new Borrowing(book, "borrower@email.de", DateTime.now().minusDays(26))));

        borrowingNotificationService.notifyBorrowers();

        ArgumentCaptor<Mail> mailArgument = ArgumentCaptor.forClass(Mail.class);
        verify(mailingService).sendMail(mailArgument.capture());

        Mail mail = mailArgument.getValue();
        assertThat(mail, is(not(nullValue())));
        assertThat(mail.getFrom(), is(equalTo("from.address@test.abc")));
        assertThat(mail.getTo(), is(equalTo("borrower@email.de")));
        assertThat(mail.getSubject(), is(equalTo("Worblehat reminder: Your borrowing period for a book ends soon!")));
        assertThat(mail.getBody(), not(isEmptyString()));
        assertThat(mail.getArguments().get("bookTitle"), is(equalTo("title")));
        assertThat(mail.getArguments().get("isbn"), is(equalTo("isbn")));
        assertThat(mail.getArguments().get("daysLeft"), is(equalTo("2")));
    }

    @Test
    public void shouldSendReminderIfBookIs2DaysAfterDueDate() {
        when(borrowingRepository.findAllBorrowings()).thenReturn(Arrays.asList(
                new Borrowing(
                        new Book("title", "author", "edition", "isbn", 1234), "borrower@email.de", DateTime.now().minusDays(30))));

        borrowingNotificationService.notifyBorrowers();

        ArgumentCaptor<Mail> mailArgument = ArgumentCaptor.forClass(Mail.class);
        verify(mailingService).sendMail(mailArgument.capture());

        Mail mail = mailArgument.getValue();
        assertThat(mail, is(not(nullValue())));
        assertThat(mail.getFrom(), is(equalTo("from.address@test.abc")));
        assertThat(mail.getTo(), is(equalTo("borrower@email.de")));
        assertThat(mail.getSubject(), is(equalTo("Worblehat reminder: Your borrowing period for a book has ended!")));
        assertThat(mail.getBody(), not(isEmptyString()));
        assertThat(mail.getArguments().get("bookTitle"), is(equalTo("title")));
        assertThat(mail.getArguments().get("isbn"), is(equalTo("isbn")));
        assertThat(mail.getArguments().get("daysOverDueDate"), is(equalTo("2")));
        assertThat(mail.getArguments().get("fee"), is(equalTo("1,00")));
    }

    @Test
    public void shouldCalculateCorrectFeeIfBookIs20DaysAfterDueDate() {
        when(borrowingRepository.findAllBorrowings()).thenReturn(Arrays.asList(
                new Borrowing(
                        new Book("title", "author", "edition", "isbn", 1234), "borrower@email.de", DateTime.now().minusDays(48))));

        borrowingNotificationService.notifyBorrowers();

        ArgumentCaptor<Mail> mailArgument = ArgumentCaptor.forClass(Mail.class);
        verify(mailingService).sendMail(mailArgument.capture());

        Mail mail = mailArgument.getValue();
        assertThat(mail, is(not(nullValue())));
        assertThat(mail.getFrom(), is(equalTo("from.address@test.abc")));
        assertThat(mail.getTo(), is(equalTo("borrower@email.de")));
        assertThat(mail.getSubject(), is(equalTo("Worblehat reminder: Your borrowing period for a book has ended!")));
        assertThat(mail.getBody(), not(isEmptyString()));
        assertThat(mail.getArguments().get("bookTitle"), is(equalTo("title")));
        assertThat(mail.getArguments().get("isbn"), is(equalTo("isbn")));
        assertThat(mail.getArguments().get("daysOverDueDate"), is(equalTo("20")));
        assertThat(mail.getArguments().get("fee"), is(equalTo("5,00")));
    }
}