package maxweb.studio;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.*;


// Note: Hibernate accepts JPQL-style queries, parses them, and converts them into SQL.
// Internally, Hibernate treats JPQL and HQL as almost interchangeable.

public class Manager {
    private static SessionFactory factory;

    public static void main(String[] args) {
        initializeSessionFactoryAndLoadConfiguration();
        createAndInsertRowsEntities();
        runHQLQueries();
        demonstrateCaching();
        lazyLoadingDemonstration();
        performCRUDOperations();
    }

    private static void createAndInsertRowsEntities() {
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        List<Author> authors = createAuthors();
        List<Book> books = createBooks(authors);
        List<Library> libraries = createLibraries(books);

        books.forEach(book -> associateBookWithAuthorAndLibraries(book, libraries));

        persistEntities(session, authors, books, libraries);

        session.getTransaction().commit();
    }

    private static void associateBookWithAuthorAndLibraries(Book book, List<Library> libraries) {
        // Maintain bidirectional Author ↔ Book (One To Many - Many To One)
        Author author = book.getAuthor();
        author.getBooks().add(book);

        // Maintain bidirectional Book ↔ Library (Many To Many)
        book.setLibraries(libraries);
        libraries.forEach(lib -> lib.getBooks().add(book));
    }

    private static void initializeSessionFactoryAndLoadConfiguration() {
        factory = new Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory();
    }

    private static List<Author> createAuthors() {
        Author orwell = new Author("George Orwell", "orwell@example.com");
        Author tolkien = new Author("J.R.R. Tolkien", "tolkien@example.com");
        Author sierra = new Author("Kathy Sierra", "sierra@techbooks.com");
        return new ArrayList<>(List.of(orwell, tolkien, sierra));
    }

    private static List<Book> createBooks(List<Author> authors) {
        Author orwell = authors.get(0);
        Author tolkien = authors.get(1);
        Author sierra = authors.get(2);

        Book b1 = new Book("1984", "Dystopian", 328, orwell);
        Book b2 = new Book("Animal Farm", "Satire", 112, orwell);
        Book b3 = new Book("The Hobbit", "Fantasy", 310, tolkien);
        Book b4 = new Book("The Fellowship of the Ring", "Fantasy", 423, tolkien);
        Book b5 = new Book("Head First Java", "Education", 720, sierra);
        Book b6 = new Book("Java Performance Tuning", "Education", 480, sierra);

        return new ArrayList<>(List.of(b1, b2, b3, b4, b5, b6));
    }

    private static List<Library> createLibraries(List<Book> books) {
        Library lib1 = new Library(
                "Central Public Library",
                "Downtown",
                new ArrayList<>(List.of(books.get(0), books.get(2), books.get(4)))
        );
        Library lib2 = new Library(
                "University Tech Library",
                "Campus North",
                new ArrayList<>(List.of(books.get(1), books.get(3), books.get(5)))
        );
        return new ArrayList<>(List.of(lib1, lib2));
    }

    private static void persistEntities(Session session, List<Author> authors, List<Book> books, List<Library> libraries) {
        authors.forEach(session::persist);
        books.forEach(session::persist);
        libraries.forEach(session::persist);
    }

    private static void runHQLQueries() {
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        String genre = "Fantasy";
        Query<Book> fantasyBooksQuery = session.createQuery("from Book where genre = :genre", Book.class);
        fantasyBooksQuery.setParameter("genre", genre);
        fantasyBooksQuery.getResultList().forEach(out::println);

        Query<Object[]> query = session.createQuery(
                "select title, pages from Book where genre = :genre1 or genre = :genre2", Object[].class
        );
        query.setParameter("genre1", "Satire");
        query.setParameter("genre2", "Dystopian");

        List<Object[]> results = query.getResultList();

        for (Object[] row : results) {
            String title = (String) row[0];
            Integer pages = (Integer) row[1];
            out.println("Title: " + title + ", Pages: " + pages);
        }

        session.getTransaction().commit();
    }

    private static void demonstrateCaching() {
        Session session = factory.getCurrentSession();
        session.beginTransaction();
        Book book = session.get(Book.class, 1);
        out.println(book);

        Book cachedBook = session.get(Book.class, 1);
        out.println(cachedBook);
        session.getTransaction().commit();
    }

    private static void lazyLoadingDemonstration() {
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // Reminder: Retrieve the Library entity by ID. Since the books collection is marked as FetchType.LAZY,
        // the associated Book entities will not be fetched from the database until explicitly accessed.
        Library library = session.get(Library.class, 1);
        Library library2 = session.byId(Library.class).getReference(2);

        session.getTransaction().commit();
    }

    private static void performCRUDOperations() {
        Session session = factory.getCurrentSession();
        session.beginTransaction();

        // 0. Insert a new author
        Author robertMartin = new Author("Robert C. Martin", "unclebob@gmail.com");
        session.persist(robertMartin);

        // 1. Insert a new book written by the new author
        Book cleanCode = new Book("Clean Code", "Education", 464, robertMartin);
        session.persist(cleanCode);

        // 2. Read: Fetch books by author name using HQL
        String targetAuthorName = "George Orwell";

        Query<Object[]> booksByOrwellQuery = session.createQuery(
                "SELECT b.title, b.genre, b.pages " +
                        "FROM Book b " +
                        "WHERE b.author.fullName = :authorName",
                Object[].class
        );

        booksByOrwellQuery.setParameter("authorName", targetAuthorName);
        List<Object[]> booksByOrwell = booksByOrwellQuery.getResultList();

        for (Object[] row : booksByOrwell) {
            String title = (String) row[0];
            String genre = (String) row[1];
            int pages = (int) row[2];

            System.out.println("Title: " + title + ", Genre: " + genre + ", Pages: " + pages);
        }

        // 3. Update: Change page count for "The Hobbit"
        Book hobbitBook = retrieveBookByTitle(session, "The Hobbit");

        if (hobbitBook != null) {
            hobbitBook.setPages(350);
            session.merge(hobbitBook);
        }

        // 4. Delete: Remove "Animal Farm" from the database
        Book animalFarmBook = retrieveBookByTitle(session, "Animal Farm");

        if (animalFarmBook != null) {
            // Removed book from all associated libraries (Many-to-Many fix)
            for (Library library : animalFarmBook.getLibraries()) {
                library.getBooks().remove(animalFarmBook);
            }

            animalFarmBook.getLibraries().clear(); // Cleaned up local reference, it is optional

            session.remove(animalFarmBook);
        }

        // 5. Aggregate: Count number of books by genre
        Query<Object[]> genreCountQuery = session.createQuery(
                "SELECT b.genre, COUNT(b) FROM Book b GROUP BY b.genre",
                Object[].class
        );

        List<Object[]> genreStats = genreCountQuery.getResultList();

        for (Object[] row : genreStats) {
            String genre = (String) row[0];
            long bookCount = (long) row[1];

            System.out.println("Genre: " + genre + ", Count: " + bookCount);
        }

        session.getTransaction().commit();
    }

    private static Book retrieveBookByTitle(Session session, String title) {
        Query<Book> bookQuery = session.createQuery(
                "FROM Book b " +
                        "WHERE b.title = :title ",
                Book.class
        );
        bookQuery.setParameter("title", title);

        return bookQuery.getSingleResult();
    }
}
