package de.codecentric.psd.worblehat.codequality.persistence.domain;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Entity implementation class for Entity: Book
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private String title;
    private String author;
    private String edition;
    private String isbn;
    private int yearOfPublication;
    private Borrowing borrowing;

    /**
     * Creates a new book instance.
     *
     * @param title             the title
     * @param author            the author
     * @param edition           the edition
     * @param isbn              the isbn
     * @param yearOfPublication the yearOfPublication
     */
    public Book(String title, String author, String edition, String isbn,
                int yearOfPublication) {
        super();
        this.title = title;
        this.author = author;
        this.edition = edition;
        this.isbn = isbn;
        this.yearOfPublication = yearOfPublication;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getEdition() {
        return edition;
    }

    public String getIsbn() {
        return isbn;
    }

    public int getYearOfPublication() {
        return yearOfPublication;
    }

    public String getBorrowerEmail() {
        return borrowing.getBorrowerEmailAddress();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setYearOfPublication(int yearOfPublication) {
        this.yearOfPublication = yearOfPublication;
    }

    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("title", title)
                .add("author", author)
                .add("edition", edition)
                .add("isbn", isbn)
                .add("yearOfPublication", yearOfPublication)
                .add("borrowing", borrowing)
                .toString();
    }
}
