package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import usyd.library_reservation_system.library_reservation_system.dto.BookCardDto;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.favorite.Favorite;
import usyd.library_reservation_system.library_reservation_system.model.favorite.FavoriteId;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.FavoriteRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testToggle_AddFavorite() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName("Test Book");
        book.setNumFavorite(5);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteService.ToggleResult result = favoriteService.toggle(userId, bookId);

        assertNotNull(result);
        assertEquals(bookId, result.bookId());
        assertEquals(userId, result.userId());
        assertTrue(result.favorited());
        assertEquals(6, result.numFavorite());

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testToggle_RemoveFavorite() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName("Test Book");
        book.setNumFavorite(5);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteService.ToggleResult result = favoriteService.toggle(userId, bookId);

        assertNotNull(result);
        assertEquals(bookId, result.bookId());
        assertEquals(userId, result.userId());
        assertFalse(result.favorited());
        assertEquals(4, result.numFavorite());

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(favoriteRepository, times(1)).deleteById(favoriteId);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testToggle_RemoveFavoriteWithZeroCount() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName("Test Book");
        book.setNumFavorite(0);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteService.ToggleResult result = favoriteService.toggle(userId, bookId);

        assertNotNull(result);
        assertEquals(0, result.numFavorite());

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testToggle_AddFavoriteWithNullCount() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookName("Test Book");
        book.setNumFavorite(null);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FavoriteService.ToggleResult result = favoriteService.toggle(userId, bookId);

        assertNotNull(result);
        assertEquals(1, result.numFavorite());
    }

    @Test
    void testToggle_BookNotFound() {
        Integer userId = 1;
        Integer bookId = 999;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            favoriteService.toggle(userId, bookId);
        });

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void testExists_True() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);

        boolean result = favoriteService.exists(userId, bookId);

        assertTrue(result);
        verify(favoriteRepository, times(1)).existsById(favoriteId);
    }

    @Test
    void testExists_False() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);

        boolean result = favoriteService.exists(userId, bookId);

        assertFalse(result);
        verify(favoriteRepository, times(1)).existsById(favoriteId);
    }

    @Test
    void testAdd_NewFavorite() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setNumFavorite(3);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.add(userId, bookId);

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testAdd_AlreadyExists() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);

        favoriteService.add(userId, bookId);

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
        verify(bookRepository, never()).findById(anyInt());
    }

    @Test
    void testAdd_WithNullFavoriteCount() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setNumFavorite(null);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.add(userId, bookId);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testAdd_BookNotFound() {
        Integer userId = 1;
        Integer bookId = 999;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        favoriteService.add(userId, bookId);

        verify(favoriteRepository, times(1)).save(any(Favorite.class));
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testRemove_ExistingFavorite() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setNumFavorite(5);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.remove(userId, bookId);

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(favoriteRepository, times(1)).deleteById(favoriteId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testRemove_NotExists() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(false);

        favoriteService.remove(userId, bookId);

        verify(favoriteRepository, times(1)).existsById(favoriteId);
        verify(favoriteRepository, never()).deleteById(any());
        verify(bookRepository, never()).findById(anyInt());
    }

    @Test
    void testRemove_WithZeroCount() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setNumFavorite(0);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.remove(userId, bookId);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testRemove_WithNullCount() {
        Integer userId = 1;
        Integer bookId = 10;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        Book book = new Book();
        book.setBookId(bookId);
        book.setNumFavorite(null);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        favoriteService.remove(userId, bookId);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void testRemove_BookNotFound() {
        Integer userId = 1;
        Integer bookId = 999;
        FavoriteId favoriteId = new FavoriteId(bookId, userId);

        when(favoriteRepository.existsById(favoriteId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteById(favoriteId);
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        favoriteService.remove(userId, bookId);

        verify(favoriteRepository, times(1)).deleteById(favoriteId);
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void testListBooksByUser_WithFavorites() {
        Integer userId = 1;
        
        FavoriteId favoriteId1 = new FavoriteId(10, userId);
        FavoriteId favoriteId2 = new FavoriteId(20, userId);
        
        Favorite favorite1 = new Favorite();
        favorite1.setId(favoriteId1);
        favorite1.setCreateTime(LocalDateTime.now());
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(favoriteId2);
        favorite2.setCreateTime(LocalDateTime.now());
        
        List<Favorite> favorites = Arrays.asList(favorite1, favorite2);

        Book book1 = new Book();
        book1.setBookId(10);
        book1.setBookName("Book 1");
        
        Book book2 = new Book();
        book2.setBookId(20);
        book2.setBookName("Book 2");
        
        List<Book> books = Arrays.asList(book1, book2);

        when(favoriteRepository.findByIdUserId(userId)).thenReturn(favorites);
        when(bookRepository.findAllById(Arrays.asList(10, 20))).thenReturn(books);

        List<Book> result = favoriteService.listBooksByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Book 1", result.get(0).getBookName());
        assertEquals("Book 2", result.get(1).getBookName());

        verify(favoriteRepository, times(1)).findByIdUserId(userId);
        verify(bookRepository, times(1)).findAllById(anyList());
    }

    @Test
    void testListBooksByUser_Empty() {
        Integer userId = 1;

        when(favoriteRepository.findByIdUserId(userId)).thenReturn(List.of());

        List<Book> result = favoriteService.listBooksByUser(userId);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(favoriteRepository, times(1)).findByIdUserId(userId);
        verify(bookRepository, never()).findAllById(anyList());
    }

    @Test
    void testListBookCardsByUser_WithFavorites() {
        Integer userId = 1;
        
        FavoriteId favoriteId1 = new FavoriteId(10, userId);
        FavoriteId favoriteId2 = new FavoriteId(20, userId);
        
        Favorite favorite1 = new Favorite();
        favorite1.setId(favoriteId1);
        favorite1.setCreateTime(LocalDateTime.now());
        
        Favorite favorite2 = new Favorite();
        favorite2.setId(favoriteId2);
        favorite2.setCreateTime(LocalDateTime.now());
        
        List<Favorite> favorites = Arrays.asList(favorite1, favorite2);

        Book book1 = new Book();
        book1.setBookId(10);
        book1.setBookName("Book 1");
        book1.setAuthor("Author 1");
        book1.setAvatar("/img/book1.jpg");
        book1.setLabelId(1);
        
        Book book2 = new Book();
        book2.setBookId(20);
        book2.setBookName("Book 2");
        book2.setAuthor("Author 2");
        book2.setAvatar("/img/book2.jpg");
        book2.setLabelId(2);
        
        List<Book> books = Arrays.asList(book1, book2);

        when(favoriteRepository.findByIdUserId(userId)).thenReturn(favorites);
        when(bookRepository.findAllById(Arrays.asList(10, 20))).thenReturn(books);

        List<BookCardDto> result = favoriteService.listBookCardsByUser(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(10, result.get(0).bookId());
        assertEquals("Book 1", result.get(0).bookName());
        assertEquals("Author 1", result.get(0).author());
        assertEquals("/img/book1.jpg", result.get(0).avatar());
        assertEquals(1, result.get(0).labelId());
        
        assertEquals(20, result.get(1).bookId());
        assertEquals("Book 2", result.get(1).bookName());

        verify(favoriteRepository, times(1)).findByIdUserId(userId);
        verify(bookRepository, times(1)).findAllById(anyList());
    }

    @Test
    void testListBookCardsByUser_Empty() {
        Integer userId = 1;

        when(favoriteRepository.findByIdUserId(userId)).thenReturn(List.of());

        List<BookCardDto> result = favoriteService.listBookCardsByUser(userId);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(favoriteRepository, times(1)).findByIdUserId(userId);
        verify(bookRepository, never()).findAllById(anyList());
    }
}

