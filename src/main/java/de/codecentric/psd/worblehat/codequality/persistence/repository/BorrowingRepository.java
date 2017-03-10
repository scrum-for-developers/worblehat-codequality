package de.codecentric.psd.worblehat.codequality.persistence.repository;

import de.codecentric.psd.worblehat.codequality.persistence.domain.Book;
import de.codecentric.psd.worblehat.codequality.persistence.domain.Borrowing;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * FAKE repository for borrowsings.
 */
@Component
public class BorrowingRepository {

    public List<Borrowing> findAllBorrowings() {
        List<Borrowing> borrowings = new ArrayList<>();

        Book book1 = new Book("Harry Potter and the Philosopher's Stone", "J. K. Rowling", "", "0747532699", 1997);
        borrowings.add(new Borrowing(book1, "borrower1@example.foo", DateTime.now().minusDays(50)));

        Book book2 = new Book("Harry Potter and the Prisoner of Azkaban", "J. K. Rowling.", "", "0747542155", 1999);
        borrowings.add(new Borrowing(book2, "borrower2@example.foo", DateTime.now().minusDays(35)));

        Book book3 = new Book("Harry Potter and the Goblet of Fire", "J. K. Rowling..", "", "074754624X", 2000);
        borrowings.add(new Borrowing(book3, "borrower3@example.foo", DateTime.now().minusDays(25)));

        Book book4 = new Book("Harry Potter and the Half-Blood Prince", "J. K. Rowling...", "", "0747581088", 2005);
        borrowings.add(new Borrowing(book4, "borrower4@example.foo", DateTime.now().minusDays(49)));

        return borrowings;
    }
}
