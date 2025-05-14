package maxweb.studio;


import jakarta.persistence.*;


import java.util.List;

@Entity
public class Library {
    @Id
    @GeneratedValue
    @Column(name = "library_id")
    private int libraryId;
    private String name;
    private String location;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "library_book",
            joinColumns = @JoinColumn(name = "library_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books;

    public Library(){}

    public Library(String name, String location, List<Book> books) {
        this.name = name;
        this.location = location;
        this.books = books;
    }

    public int getLibraryId() {
        return libraryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    @Override
    public String toString() {
        return "Library{" +
                "libraryId=" + libraryId +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
