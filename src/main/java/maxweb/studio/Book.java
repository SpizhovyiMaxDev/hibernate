package maxweb.studio;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Book {
    @Id
    @GeneratedValue
    @Column(name = "book_id")
    private int bookId;
    private String title;
    private String genre;
    private int pages;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private Author author;

    @ManyToMany(mappedBy = "books")
    private List<Library> libraries;

    @Transient
    private boolean isLong;

    public Book(){}

    public Book(String title, String genre, int pages, Author author) {
        this.title = title;
        this.genre = genre;
        this.pages = pages;
        this.author = author;
        this.isLong = pages > 400;
    }

    public int getBookId() {
        return bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
        this.isLong = pages > 400;
    }

    public Author getAuthor() {
        return author;
    }

    public void setLibraries(List<Library> libraries){
        this.libraries = libraries;
    }

    public List<Library> getLibraries() { return libraries; }

    @Override
    public String toString() {
        return "Book{" +
                "bookId=" + bookId +
                ", title='" + title + '\'' +
                ", genre='" + genre + '\'' +
                ", pages=" + pages +
                ", authorId=" + (author != null ? author.getAuthorId() : null) +
                '}';
    }
}
