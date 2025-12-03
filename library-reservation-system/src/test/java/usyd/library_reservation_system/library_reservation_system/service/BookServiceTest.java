package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.BookSimpleProjection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllBooks() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        Book book2 = createTestBook(2, "Python Basics", "Li Si", 5);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findAll()).thenReturn(mockBooks);

        List<Book> result = bookService.getAllBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetBookById_Exists() {
        Book book = createTestBook(1, "Java Programming", "Zhang San", 10);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        Optional<Book> result = bookService.getBookById(1);

        assertTrue(result.isPresent());
        assertEquals("Java Programming", result.get().getBookName());
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void testGetBookById_NotExists() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        Optional<Book> result = bookService.getBookById(999);

        assertFalse(result.isPresent());
        verify(bookRepository, times(1)).findById(999);
    }

    @Test
    void testCreateBook_WithNullQuantity() {
        Book book = new Book();
        book.setBookName("New Book");
        book.setAuthor("Author");
        book.setQuantity(null);
        book.setNumFavorite(null);
        book.setNumReservation(null);
        book.setLabelId(1);

        Book savedBook = createTestBook(1, "New Book", "Author", 0);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook(book);

        assertNotNull(result);
        assertEquals(0, result.getQuantity());
        assertEquals(0, result.getNumFavorite());
        assertEquals(0, result.getNumReservation());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testCreateBook_WithAllFields() {
        Book book = createTestBook(null, "New Book", "Author", 5);
        book.setNumFavorite(3);
        book.setNumReservation(2);

        Book savedBook = createTestBook(1, "New Book", "Author", 5);
        savedBook.setNumFavorite(3);
        savedBook.setNumReservation(2);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook(book);

        assertNotNull(result);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBook_Exists() {
        Book existingBook = createTestBook(1, "Old Book Name", "Old Author", 5);
        Book updateDetails = createTestBook(null, "New Book Name", "New Author", 10);
        updateDetails.setDescription("New Description");
        updateDetails.setLabelId(2);
        updateDetails.setAvatar("/img/test.jpg");

        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        Book result = bookService.updateBook(1, updateDetails);

        assertNotNull(result);
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBook_NotExists() {
        Book updateDetails = createTestBook(null, "New Book Name", "New Author", 10);
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        Book result = bookService.updateBook(999, updateDetails);

        assertNull(result);
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testDeleteBook_Exists() {
        when(bookRepository.existsById(1)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(1);

        boolean result = bookService.deleteBook(1);

        assertTrue(result);
        verify(bookRepository, times(1)).existsById(1);
        verify(bookRepository, times(1)).deleteById(1);
    }

    @Test
    void testDeleteBook_NotExists() {
        when(bookRepository.existsById(999)).thenReturn(false);

        boolean result = bookService.deleteBook(999);

        assertFalse(result);
        verify(bookRepository, times(1)).existsById(999);
        verify(bookRepository, never()).deleteById(anyInt());
    }

    @Test
    void testSearchBooksByName() {
        Book book1 = createTestBook(1, "Java Programming Guide", "Zhang San", 10);
        Book book2 = createTestBook(2, "Advanced Java Programming", "Li Si", 5);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findByBookNameContaining("Java")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooksByName("Java");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findByBookNameContaining("Java");
    }

    @Test
    void testSearchBooksByAuthor() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        Book book2 = createTestBook(2, "Python Basics", "Zhang San", 5);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findByAuthorContaining("Zhang San")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooksByAuthor("Zhang San");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findByAuthorContaining("Zhang San");
    }

    @Test
    void testGetBooksByLabelId() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        book1.setLabelId(1);
        Book book2 = createTestBook(2, "Python Basics", "Li Si", 5);
        book2.setLabelId(1);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findByLabelId(1)).thenReturn(mockBooks);

        List<Book> result = bookService.getBooksByLabelId(1);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findByLabelId(1);
    }

    @Test
    void testGetAvailableBooks() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        Book book2 = createTestBook(2, "Python Basics", "Li Si", 5);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findByQuantityGreaterThan(0)).thenReturn(mockBooks);

        List<Book> result = bookService.getAvailableBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findByQuantityGreaterThan(0);
    }

    @Test
    void testSearchBooks() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        Book book2 = createTestBook(2, "Python Basics", "Java Expert", 5);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.searchByKeyword("Java")).thenReturn(mockBooks);

        List<Book> result = bookService.searchBooks("Java");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).searchByKeyword("Java");
    }

    @Test
    void testGetMostPopularBooks() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        book1.setNumFavorite(100);
        Book book2 = createTestBook(2, "Python Basics", "Li Si", 5);
        book2.setNumFavorite(50);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findMostPopularBooks()).thenReturn(mockBooks);

        List<Book> result = bookService.getMostPopularBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetMostReservedBooks() {
        Book book1 = createTestBook(1, "Java Programming", "Zhang San", 10);
        book1.setNumReservation(80);
        Book book2 = createTestBook(2, "Python Basics", "Li Si", 5);
        book2.setNumReservation(30);
        List<Book> mockBooks = Arrays.asList(book1, book2);

        when(bookRepository.findMostReservedBooks()).thenReturn(mockBooks);

        List<Book> result = bookService.getMostReservedBooks();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(bookRepository, times(1)).findMostReservedBooks();
    }

    @Test
    void testUpdateBookQuantity_Exists() {
        Book existingBook = createTestBook(1, "Java Programming", "Zhang San", 5);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        Book result = bookService.updateBookQuantity(1, 20);

        assertNotNull(result);
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testUpdateBookQuantity_NotExists() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        Book result = bookService.updateBookQuantity(999, 20);

        assertNull(result);
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testIncrementFavoriteCount_Exists() {
        Book existingBook = createTestBook(1, "Java Programming", "Zhang San", 10);
        existingBook.setNumFavorite(5);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        Book result = bookService.incrementFavoriteCount(1);

        assertNotNull(result);
        assertEquals(6, result.getNumFavorite());
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testIncrementFavoriteCount_NotExists() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        Book result = bookService.incrementFavoriteCount(999);

        assertNull(result);
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testIncrementReservationCount_Exists() {
        Book existingBook = createTestBook(1, "Java Programming", "Zhang San", 10);
        existingBook.setNumReservation(3);
        when(bookRepository.findById(1)).thenReturn(Optional.of(existingBook));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            return book;
        });

        Book result = bookService.incrementReservationCount(1);

        assertNotNull(result);
        assertEquals(4, result.getNumReservation());
        verify(bookRepository, times(1)).findById(1);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testIncrementReservationCount_NotExists() {
        when(bookRepository.findById(999)).thenReturn(Optional.empty());

        Book result = bookService.incrementReservationCount(999);

        assertNull(result);
        verify(bookRepository, times(1)).findById(999);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testSearchByLabelName() {
        BookSimpleProjection projection1 = mock(BookSimpleProjection.class);
        when(projection1.getBookId()).thenReturn(1);
        when(projection1.getBookName()).thenReturn("Java Programming");

        BookSimpleProjection projection2 = mock(BookSimpleProjection.class);
        when(projection2.getBookId()).thenReturn(2);
        when(projection2.getBookName()).thenReturn("Python Basics");

        List<BookSimpleProjection> mockProjections = Arrays.asList(projection1, projection2);

        when(bookRepository.searchBooksByLabelNameWithPriority("Technology")).thenReturn(mockProjections);

        List<BookService.BookSimpleDto> result = bookService.searchByLabelName("Technology");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).bookId());
        assertEquals("Java Programming", result.get(0).bookName());
        assertEquals(2, result.get(1).bookId());
        assertEquals("Python Basics", result.get(1).bookName());
        verify(bookRepository, times(1)).searchBooksByLabelNameWithPriority("Technology");
    }

    // Helper method to create test Book objects
    private Book createTestBook(Integer id, String name, String author, Integer quantity) {
        Book book = new Book();
        book.setBookId(id);
        book.setBookName(name);
        book.setAuthor(author);
        book.setQuantity(quantity);
        book.setLabelId(1);
        book.setNumFavorite(0);
        book.setNumReservation(0);
        return book;
    }
}

