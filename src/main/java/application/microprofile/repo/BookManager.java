package application.microprofile.repo;

import application.microprofile.model.Book;

import javax.enterprise.context.ApplicationScoped;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class BookManager {

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
    private AtomicInteger bookIdGenerator = new AtomicInteger(0);

    private ConcurrentMap<String, Book> inMemoryStore = new ConcurrentHashMap<>();

    public BookManager() {
        Book book = new Book();
        book.setId(getNextId());
        book.setName("Building Microservice With Eclipse MicroProfile");
        book.setIsbn("1");
        book.setAuthor("David");
        book.setPages(420);
        inMemoryStore.put(book.getId(), book);
        
        Book book1 = new Book();
        book1.setId(getNextId());
        book1.setName("Getting Started with Open Liberty");
        book1.setIsbn("1");
        book1.setAuthor("kevin");
        book1.setPages(280);
        inMemoryStore.put(book1.getId(), book1);
    }

    private String getNextId() {
        String date = LocalDate.now().format(formatter);
        return String.format("%04d-%s", bookIdGenerator.incrementAndGet(), date);
    }

    public String add(Book book) {
        String id = getNextId();
        book.setId(id);
        inMemoryStore.put(id, book);
        return id;
    }

    public Book get(String id) {
        return inMemoryStore.get(id);
    }

    public List<Book> getAll() {
        List<Book> books = new ArrayList<>();
        books.addAll(inMemoryStore.values());
        System.out.println("Number of books in catalog: " + books.size());
        return books;
    }
    
    public String sendErrorMessage(String message) {
    	  String json = "{\"ERROR\":\"" + message + "\"}";
		  return json;
    }
}
