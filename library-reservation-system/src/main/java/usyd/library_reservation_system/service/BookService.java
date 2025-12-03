package usyd.library_reservation_system.library_reservation_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;


    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }


    public Optional<Book> getBookById(Integer id) {
        return bookRepository.findById(id);
    }


    public Book createBook(Book book) {
        if (book.getQuantity() == null) {
            book.setQuantity(0);
        }
        if (book.getNumFavorite() == null) {
            book.setNumFavorite(0);
        }
        if (book.getNumReservation() == null) {
            book.setNumReservation(0);
        }
        return bookRepository.save(book);
    }


    public Book updateBook(Integer id, Book bookDetails) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setBookName(bookDetails.getBookName());
            book.setAuthor(bookDetails.getAuthor());
            book.setDescription(bookDetails.getDescription());
            book.setQuantity(bookDetails.getQuantity());
            book.setLabelId(bookDetails.getLabelId());
            book.setAvatar(bookDetails.getAvatar());
            return bookRepository.save(book);
        }
        return null;
    }


    public boolean deleteBook(Integer id) {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
            return true;
        }
        return false;
    }


    public List<Book> searchBooksByName(String name) {
        return bookRepository.findByBookNameContaining(name);
    }


    public List<Book> searchBooksByAuthor(String author) {
        return bookRepository.findByAuthorContaining(author);
    }


    public List<Book> getBooksByLabelId(Integer labelId) {
        return bookRepository.findByLabelId(labelId);
    }


    public List<Book> getAvailableBooks() {
        return bookRepository.findByQuantityGreaterThan(0);
    }


    public List<Book> searchBooks(String keyword) {
        return bookRepository.searchByKeyword(keyword);
    }


    public List<Book> getMostPopularBooks() {
        return bookRepository.findMostPopularBooks();
    }


    public List<Book> getMostReservedBooks() {
        return bookRepository.findMostReservedBooks();
    }


    public Book updateBookQuantity(Integer id, Integer quantity) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setQuantity(quantity);
            return bookRepository.save(book);
        }
        return null;
    }


    public Book incrementFavoriteCount(Integer id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setNumFavorite(book.getNumFavorite() + 1);
            return bookRepository.save(book);
        }
        return null;
    }


    public Book incrementReservationCount(Integer id) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setNumReservation(book.getNumReservation() + 1);
            return bookRepository.save(book);
        }
        return null;
    }


    public record BookSimpleDto(Integer bookId, String bookName) {}

    public List<BookSimpleDto> searchByLabelName(String q) {
        var rows = bookRepository.searchBooksByLabelNameWithPriority(q);
        return rows.stream()
                .map(p -> new BookSimpleDto(p.getBookId(), p.getBookName()))
                .toList();
    }

}