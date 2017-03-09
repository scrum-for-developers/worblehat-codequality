package de.codecentric.psd.worblehat.codequality.persistence.domain;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * Borrowing Entity
 */
public class Borrowing implements Serializable {

	private static final long serialVersionUID = 1L;

	private long id; // NOSONAR
	private String borrowerEmailAddress;
	private Date borrowDate;
	private Book borrowedBook;

	public String getBorrowerEmailAddress() {
		return borrowerEmailAddress;
	}

	public Date getBorrowDate() {
		return borrowDate;
	}

	public Book getBorrowedBook() {
		return borrowedBook;
	}

	/**
	 * @param book
	 * The borrowed book
	 * @param borrowerEmailAddress
	 * The borrowers e-mail Address
	 * @param borrowDate
	 * The borrow date
     */
	public Borrowing(Book book, String borrowerEmailAddress, DateTime borrowDate) {
		super();
		this.borrowedBook = book;
		this.borrowerEmailAddress = borrowerEmailAddress;
		this.borrowDate = borrowDate.toDate();
	}
}
