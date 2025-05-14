package maxweb.studio;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Author {
    @Id
    @GeneratedValue
    @Column(name = "author_id")
    private int authorId;

    @Column(name = "full_name")
    private String fullName;
    private String email;

    @OneToMany(mappedBy = "author")
    private List<Book> books = new ArrayList<>();

    public Author(){}

    public Author(String fullName, String email) {
        this.fullName = fullName;
        this.email = email;
    }

    public int getAuthorId() {
        return authorId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "Author{" +
                "authorId=" + authorId +
                ", fullName=" + fullName +
                ", email='" + email + '\'' +
                '}';
    }
}
