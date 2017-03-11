package de.codecentric.psd.worblehat.codequality.service;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;
import de.codecentric.psd.worblehat.codequality.configuration.MailSettings;
import de.codecentric.psd.worblehat.codequality.configuration.SMTPSettings;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Book;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Borrowing;
import de.codecentric.psd.worblehat.codequality.persistence.repository.BorrowingRepository;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class BorrowingNotificationServiceTest {

    private SimpleSmtpServer server;
    private BorrowingNotificationService service;
    private BorrowingRepository repo;

    @Before
    public void setUp() throws Exception {
        SMTPSettings smtpSettings = new SMTPSettings();
        smtpSettings.setHost("127.0.0.1");
        smtpSettings.setPort("10025");

        MailSettings mailSettings = new MailSettings();
        mailSettings.setFrom("from.address@test.abc");

        repo = Mockito.mock(BorrowingRepository.class);
        service = new BorrowingNotificationService(smtpSettings, mailSettings, repo);
        server = SimpleSmtpServer.start(10025);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void test_notification_of_no_borrowers_if_nobody_has_borrowed_a_book() throws Exception {
        when(repo.findAllBorrowings()).thenReturn(Collections.emptyList());
        service.notify_borrowers_via_email_if_their_book_boorowings_last_longer_than_the_allowed_limit();
        assertThat(server.getReceivedEmails().size(), is(0));
    }

    @Test
    public void test_notification_of_borrower_if_borrower_has_to_return_it_in_2_days() {
        when(repo.findAllBorrowings()).thenReturn(Arrays.asList(
                new Borrowing(
                        new Book("title", "author", "edition", "isbn", 1234), "borrower@email.de", DateTime.now().minusDays(26))));

        service.notify_borrowers_via_email_if_their_book_boorowings_last_longer_than_the_allowed_limit();

        assertThat(server.getReceivedEmails().size(), is(1));
        SmtpMessage smtpMessage = server.getReceivedEmails().get(0);
        assertThat(smtpMessage.getHeaderValue("From"), is(equalTo("from.address@test.abc")));
        assertThat(smtpMessage.getHeaderValue("To"), is(equalTo("borrower@email.de")));
        assertThat(smtpMessage.getHeaderValue("Subject"), is(equalTo("Worblehat reminder: Your borrowing period for a book ends soon!")));
        assertThat(smtpMessage.getBody(), containsString("Please return the book in the next 2 days"));
    }

    @Test
    public void test_notification_of_borrower_if_borrower_had_to_return_it_2_days_ago() {
        when(repo.findAllBorrowings()).thenReturn(Arrays.asList(
                new Borrowing(
                        new Book("title", "author", "edition", "isbn", 1234), "borrower@email.de", DateTime.now().minusDays(30))));

        service.notify_borrowers_via_email_if_their_book_boorowings_last_longer_than_the_allowed_limit();

        assertThat(server.getReceivedEmails().size(), is(1));
        SmtpMessage smtpMessage = server.getReceivedEmails().get(0);
        assertThat(smtpMessage.getHeaderValue("From"), is(equalTo("from.address@test.abc")));
        assertThat(smtpMessage.getHeaderValue("To"), is(equalTo("borrower@email.de")));
        assertThat(smtpMessage.getHeaderValue("Subject"), is(equalTo("Worblehat reminder: Your borrowing period for a book has ended!")));
        assertThat(smtpMessage.getBody(), containsString("your borrowing period for the book 'title' (ISBN: isbn) ended 2 days ago."));
        assertThat(smtpMessage.getBody(), containsString("For the delay we charge you a fee of 1,00 =E2=82=AC."));
    }

    @Test
    public void test_notification_of_borrower_if_borrower_had_to_return_it_20_days_ago() {
        when(repo.findAllBorrowings()).thenReturn(Arrays.asList(
                new Borrowing(
                        new Book("title", "author", "edition", "isbn", 1234), "borrower@email.de", DateTime.now().minusDays(48))));

        service.notify_borrowers_via_email_if_their_book_boorowings_last_longer_than_the_allowed_limit();

        assertThat(server.getReceivedEmails().size(), is(1));
        SmtpMessage smtpMessage = server.getReceivedEmails().get(0);
        assertThat(smtpMessage.getHeaderValue("From"), is(equalTo("from.address@test.abc")));
        assertThat(smtpMessage.getHeaderValue("To"), is(equalTo("borrower@email.de")));
        assertThat(smtpMessage.getHeaderValue("Subject"), is(equalTo("Worblehat reminder: Your borrowing period for a book has ended!")));
        assertThat(smtpMessage.getBody(), containsString("your borrowing period for the book 'title' (ISBN: isbn) ended 20 days ago."));
        assertThat(smtpMessage.getBody(), containsString("For the delay we charge you a fee of 5,00 =E2=82=AC."));
    }
}