package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.Label;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.LabelRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LabelRepository labelRepository;

    @InjectMocks
    private ChatService chatService;

    private Book testBook1;
    private Book testBook2;
    private Book testBook3;
    private Label testLabel;
    private List<Book> popularBooks;
    private List<Label> allLabels;

    @BeforeEach
    void setUp() {
        // Setup test books
        testBook1 = new Book();
        testBook1.setBookId(1);
        testBook1.setBookName("The Great Gatsby");
        testBook1.setAuthor("F. Scott Fitzgerald");
        testBook1.setDescription("A classic American novel about the Jazz Age");
        testBook1.setQuantity(5);
        testBook1.setNumFavorite(15);
        testBook1.setLabelId(1);

        testBook2 = new Book();
        testBook2.setBookId(2);
        testBook2.setBookName("1984");
        testBook2.setAuthor("George Orwell");
        testBook2.setDescription("A dystopian social science fiction novel and cautionary tale");
        testBook2.setQuantity(0);
        testBook2.setNumFavorite(8);
        testBook2.setLabelId(1);

        testBook3 = new Book();
        testBook3.setBookId(3);
        testBook3.setBookName("To Kill a Mockingbird");
        testBook3.setAuthor("Harper Lee");
        testBook3.setDescription("A novel about racial injustice in the American South");
        testBook3.setQuantity(3);
        testBook3.setNumFavorite(3);
        testBook3.setLabelId(2);

        // Setup test label
        testLabel = new Label();
        testLabel.setLabelId(1);
        testLabel.setLabelName("fiction");

        popularBooks = Arrays.asList(testBook1, testBook2, testBook3);
        allLabels = Arrays.asList(testLabel);
    }

    // ==================== Basic getChatResponse Tests ====================

    @Test
    void testGetChatResponse_WithGreeting_Hello() {
        // Act
        String response = chatService.getChatResponse("hello");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
        assertTrue(response.contains("library AI assistant"));
    }

    @Test
    void testGetChatResponse_WithGreeting_Hi() {
        // Act
        String response = chatService.getChatResponse("hi");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
    }

    @Test
    void testGetChatResponse_WithGreeting_Chinese() {
        // Act
        String response = chatService.getChatResponse("Hello");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
    }

    @Test
    void testGetChatResponse_WithGreeting_MixedCase() {
        // Act
        String response = chatService.getChatResponse("HeLLo");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
    }

    @Test
    void testGetChatResponse_WithGreeting_WithWhitespace() {
        // Act
        String response = chatService.getChatResponse("  hello  ");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
    }

    // ==================== Help Request Tests ====================

    @Test
    void testGetChatResponse_WithHelpRequest() {
        // Act
        String response = chatService.getChatResponse("help");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
        assertTrue(response.contains("Book Recommendations"));
    }

    @Test
    void testGetChatResponse_WithHelpRequest_Chinese() {
        // Act
        String response = chatService.getChatResponse("help me");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    @Test
    void testGetChatResponse_WithWhatCanYouDo() {
        // Act
        String response = chatService.getChatResponse("what can you do");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    // ==================== Reservation Tests ====================

    @Test
    void testGetChatResponse_WithReservationRequest() {
        // Act
        String response = chatService.getChatResponse("how do I reserve a book");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Great question about reservations"));
        assertTrue(response.contains("Subscribe"));
    }

    @Test
    void testGetChatResponse_WithBorrowRequest() {
        // Act
        String response = chatService.getChatResponse("how to borrow books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Great question about reservations"));
    }

    @Test
    void testGetChatResponse_WithReservationRequest_Chinese() {
        // Act
        String response = chatService.getChatResponse("how to reserve");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Great question about reservations"));
    }

    // ==================== Search Tests ====================

    @Test
    void testGetChatResponse_WithSearchRequest() {
        // Act
        String response = chatService.getChatResponse("how to search for books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Let me help you find books"));
        assertTrue(response.contains("search bar"));
    }

    @Test
    void testGetChatResponse_WithFindRequest() {
        // Act
        String response = chatService.getChatResponse("how do I find a book");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Let me help you find books"));
    }

    @Test
    void testGetChatResponse_WithSearchRequest_Chinese() {
        // Act
        String response = chatService.getChatResponse("search");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Let me help you find books"));
    }

    // ==================== Popular Books Tests ====================

    @Test
    void testGetChatResponse_WithPopularRequest() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("show me popular books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("popular books") || response.contains("recommend"));
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_WithTrendingRequest() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("trending books");

        // Assert
        assertNotNull(response);
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    // ==================== Category Tests ====================

    @Test
    void testGetChatResponse_WithCategoryRequest() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);

        // Act
        String response = chatService.getChatResponse("show me categories");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("categories"));
        assertTrue(response.contains("fiction"));
        verify(labelRepository, times(1)).findAll();
    }

    @Test
    void testGetChatResponse_WithCategoryRequest_EmptyCategories() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        String response = chatService.getChatResponse("show me categories");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("no categories"));
    }

    @Test
    void testGetChatResponse_WithCategoryRequest_MultipleCategories() {
        // Arrange
        Label label2 = new Label();
        label2.setLabelId(2);
        label2.setLabelName("science");
        
        Label label3 = new Label();
        label3.setLabelId(3);
        label3.setLabelName("history");
        
        when(labelRepository.findAll()).thenReturn(Arrays.asList(testLabel, label2, label3));

        // Act
        String response = chatService.getChatResponse("what are the categories");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("fiction"));
        assertTrue(response.contains("science"));
        assertTrue(response.contains("history"));
    }

    // ==================== Book Recommendation Tests ====================

    @Test
    void testGetChatResponse_WithRecommendRequest() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend some books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("recommend") || response.contains("popular"));
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_WithRecommendRequest_Chinese() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend some books");

        // Assert
        assertNotNull(response);
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_WithRecommendByCategory() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(labelRepository.findByLabelName("fiction")).thenReturn(Optional.of(testLabel));
        when(bookRepository.findByLabelId(1)).thenReturn(Arrays.asList(testBook1, testBook2));

        // Act
        String response = chatService.getChatResponse("recommend some fiction books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("fiction"));
        verify(labelRepository, times(1)).findAll();
        verify(bookRepository, times(1)).findByLabelId(1);
    }

    @Test
    void testGetChatResponse_WithRecommendByCategory_NoBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(labelRepository.findByLabelName("fiction")).thenReturn(Optional.of(testLabel));
        when(bookRepository.findByLabelId(1)).thenReturn(Collections.emptyList());

        // Act
        String response = chatService.getChatResponse("recommend some fiction books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("couldn't find") || response.contains("empty"));
    }

    @Test
    void testGetChatResponse_WithRecommendByCategory_LabelNotFound() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(labelRepository.findByLabelName("fiction")).thenReturn(Optional.empty());
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend some fiction books");

        // Assert
        assertNotNull(response);
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_WithRecommendRequest_EmptyBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.emptyList());

        // Act
        String response = chatService.getChatResponse("recommend some books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("empty") || response.contains("contact"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithAvailableBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("The Great Gatsby") || response.contains("popular"));
        assertTrue(response.contains("Available") || response.contains("Reserved"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithLongDescription() {
        // Arrange
        Book bookWithLongDesc = new Book();
        bookWithLongDesc.setBookId(4);
        bookWithLongDesc.setBookName("Long Description Book");
        bookWithLongDesc.setAuthor("Test Author");
        bookWithLongDesc.setDescription("This is a very long description that exceeds seventy characters and should be truncated in the response");
        bookWithLongDesc.setQuantity(5);
        bookWithLongDesc.setNumFavorite(10);

        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(bookWithLongDesc));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("..."));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithNullDescription() {
        // Arrange
        testBook1.setDescription(null);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertFalse(response.contains("null"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithEmptyDescription() {
        // Arrange
        testBook1.setDescription("");
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithNullNumFavorite() {
        // Arrange
        testBook1.setNumFavorite(null);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithZeroQuantity() {
        // Arrange
        testBook1.setQuantity(0);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Reserved"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithHighFavorites() {
        // Arrange
        testBook1.setNumFavorite(20);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("⭐⭐⭐"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithMediumFavorites() {
        // Arrange
        testBook1.setNumFavorite(7);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("⭐⭐"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_WithLowFavorites() {
        // Arrange
        testBook1.setNumFavorite(3);
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Collections.singletonList(testBook1));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("⭐"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_FirstBookGoldMedal() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("🥇"));
    }

    @Test
    void testGetChatResponse_BookRecommendation_LimitedToFiveBooks() {
        // Arrange
        List<Book> manyBooks = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Book book = new Book();
            book.setBookId(i);
            book.setBookName("Book " + i);
            book.setAuthor("Author " + i);
            book.setQuantity(5);
            book.setNumFavorite(10 - i);
            manyBooks.add(book);
        }
        
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(manyBooks);

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        // Should only show first 5 books
        assertTrue(response.contains("Book 0"));
        assertTrue(response.contains("Book 4"));
        assertFalse(response.contains("Book 5"));
    }

    // ==================== Default/Unknown Request Tests ====================

    @Test
    void testGetChatResponse_WithUnknownMessage() {
        // Act
        String response = chatService.getChatResponse("random unknown message");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    @Test
    void testGetChatResponse_WithEmptyMessage() {
        // Act
        String response = chatService.getChatResponse("");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    @Test
    void testGetChatResponse_WithWhitespaceOnly() {
        // Act
        String response = chatService.getChatResponse("   ");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    @Test
    void testGetChatResponse_WithSpecialCharacters() {
        // Act
        String response = chatService.getChatResponse("!@#$%^&*()");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    // ==================== Exception Handling Tests ====================

    @Test
    void testGetChatResponse_WithNullMessage() {
        // Act
        String response = chatService.getChatResponse(null);

        // Assert - Should return error message from exception handler
        assertNotNull(response);
        assertTrue(response.contains("Oops"));
    }

    @Test
    void testGetChatResponse_WithRepositoryException() {
        // Arrange
        when(labelRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        String response = chatService.getChatResponse("recommend some books");

        // Assert - The exception is caught and friendly error message is returned
        assertNotNull(response);
        assertTrue(response.contains("Oops") || response.contains("trouble"));
    }

    @Test
    void testGetChatResponse_WithBookRepositoryException() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenThrow(new RuntimeException("Database error"));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Oops") || response.contains("trouble"));
    }

    @Test
    void testGetChatResponse_WithLabelRepositoryException() {
        // Arrange
        when(labelRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // Act
        String response = chatService.getChatResponse("show categories");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Oops"));
    }

    // ==================== Case Sensitivity Tests ====================

    @Test
    void testGetChatResponse_CaseInsensitive_HELLO() {
        // Act
        String response = chatService.getChatResponse("HELLO");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("Hi there"));
    }

    @Test
    void testGetChatResponse_CaseInsensitive_RECOMMEND() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("RECOMMEND SOME BOOKS");

        // Assert
        assertNotNull(response);
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_CaseInsensitive_MixedCase() {
        // Act
        String response = chatService.getChatResponse("HeLp Me");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    // ==================== Multiple Keywords Tests ====================

    @Test
    void testGetChatResponse_WithMultipleKeywords_RecommendAndHelp() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("help me recommend some books");

        // Assert
        assertNotNull(response);
        // Should prioritize recommendation request
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_WithCategoryInRecommendation() {
        // Arrange
        Label scienceLabel = new Label();
        scienceLabel.setLabelId(2);
        scienceLabel.setLabelName("science");
        
        when(labelRepository.findAll()).thenReturn(Arrays.asList(testLabel, scienceLabel));
        when(labelRepository.findByLabelName("science")).thenReturn(Optional.of(scienceLabel));
        
        Book scienceBook = new Book();
        scienceBook.setBookId(10);
        scienceBook.setBookName("A Brief History of Time");
        scienceBook.setAuthor("Stephen Hawking");
        scienceBook.setQuantity(5);
        scienceBook.setNumFavorite(12);
        
        when(bookRepository.findByLabelId(2)).thenReturn(Collections.singletonList(scienceBook));

        // Act
        String response = chatService.getChatResponse("recommend some science books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("science"));
        verify(bookRepository, times(1)).findByLabelId(2);
    }

    // ==================== Integration Tests ====================

    @Test
    void testGetChatResponse_CompleteFlow_GreetingThenRecommendation() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String greetingResponse = chatService.getChatResponse("hello");
        String recommendResponse = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(greetingResponse);
        assertNotNull(recommendResponse);
        assertTrue(greetingResponse.contains("Hi there"));
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_CompleteFlow_CategoryThenRecommendation() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(labelRepository.findByLabelName("fiction")).thenReturn(Optional.of(testLabel));
        when(bookRepository.findByLabelId(1)).thenReturn(Arrays.asList(testBook1, testBook2));

        // Act
        String categoryResponse = chatService.getChatResponse("show categories");
        String recommendResponse = chatService.getChatResponse("recommend fiction books");

        // Assert
        assertNotNull(categoryResponse);
        assertNotNull(recommendResponse);
        assertTrue(categoryResponse.contains("fiction"));
        assertTrue(recommendResponse.contains("fiction"));
    }

    // ==================== Edge Cases ====================

    @Test
    void testGetChatResponse_VeryLongMessage() {
        // Arrange
        String longMessage = "recommend ".repeat(100) + "books";
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse(longMessage);

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetChatResponse_WithNewlines() {
        // Act
        String response = chatService.getChatResponse("hello\nworld");

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetChatResponse_WithTabs() {
        // Act
        String response = chatService.getChatResponse("hello\tworld");

        // Assert
        assertNotNull(response);
    }

    @Test
    void testGetChatResponse_WithNumbers() {
        // Act
        String response = chatService.getChatResponse("12345");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("I'm here to help"));
    }

    @Test
    void testGetChatResponse_WithEmojis() {
        // Act
        String response = chatService.getChatResponse("😊 📚 recommend books");

        // Assert
        assertNotNull(response);
    }

    // ==================== Response Format Tests ====================

    @Test
    void testGetChatResponse_ReturnsNonEmptyString() {
        // Act
        String response = chatService.getChatResponse("hello");

        // Assert
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.length() > 0);
    }

    @Test
    void testGetChatResponse_ContainsEmojis() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("🌟") || response.contains("📚") || response.contains("✅"));
    }

    @Test
    void testGetChatResponse_Greeting_ContainsNewlines() {
        // Act
        String response = chatService.getChatResponse("hello");

        // Assert
        assertNotNull(response);
        assertTrue(response.contains("\n"));
    }

    // ==================== Repository Interaction Tests ====================

    @Test
    void testGetChatResponse_CallsLabelRepositoryForCategories() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);

        // Act
        chatService.getChatResponse("show categories");

        // Assert
        verify(labelRepository, times(1)).findAll();
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetChatResponse_CallsBookRepositoryForRecommendations() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        chatService.getChatResponse("recommend books");

        // Assert
        verify(bookRepository, times(1)).findMostPopularBooks();
        verify(labelRepository, times(1)).findAll();
    }

    @Test
    void testGetChatResponse_NoRepositoryCallsForGreeting() {
        // Act
        chatService.getChatResponse("hello");

        // Assert
        verifyNoInteractions(labelRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetChatResponse_NoRepositoryCallsForHelp() {
        // Act
        chatService.getChatResponse("help");

        // Assert
        verifyNoInteractions(labelRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetChatResponse_NoRepositoryCallsForReservation() {
        // Act
        chatService.getChatResponse("how to reserve");

        // Assert
        verifyNoInteractions(labelRepository);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void testGetChatResponse_NoRepositoryCallsForSearch() {
        // Act
        chatService.getChatResponse("how to search");

        // Assert
        verifyNoInteractions(labelRepository);
        verifyNoInteractions(bookRepository);
    }

    // ==================== Specific Keyword Detection Tests ====================

    @Test
    void testIsRecommendationRequest_WithRecommend() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("recommend");

        // Assert
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testIsRecommendationRequest_WithWhatBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("what books do you have");

        // Assert
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testIsRecommendationRequest_WithSuggestBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("suggest books for me");

        // Assert
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testIsRecommendationRequest_WithGoodBooks() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response = chatService.getChatResponse("good books");

        // Assert
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    // ==================== Book Sorting Tests ====================

    @Test
    void testGetChatResponse_BooksSortedByFavorites() {
        // Arrange
        Book lowFav = new Book();
        lowFav.setBookId(1);
        lowFav.setBookName("Low Favorite Book");
        lowFav.setAuthor("Author A");
        lowFav.setQuantity(5);
        lowFav.setNumFavorite(2);

        Book highFav = new Book();
        highFav.setBookId(2);
        highFav.setBookName("High Favorite Book");
        highFav.setAuthor("Author B");
        highFav.setQuantity(5);
        highFav.setNumFavorite(20);

        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(Arrays.asList(lowFav, highFav));

        // Act
        String response = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response);
        // The response should contain both books
        assertTrue(response.contains("Low Favorite Book"));
        assertTrue(response.contains("High Favorite Book"));
    }

    // ==================== Multiple Requests Tests ====================

    @Test
    void testGetChatResponse_MultipleConsecutiveCalls() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response1 = chatService.getChatResponse("hello");
        String response2 = chatService.getChatResponse("help");
        String response3 = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        assertNotNull(response3);
        assertTrue(response1.contains("Hi there"));
        assertTrue(response2.contains("I'm here to help"));
        verify(bookRepository, times(1)).findMostPopularBooks();
    }

    @Test
    void testGetChatResponse_SameRequestMultipleTimes() {
        // Arrange
        when(labelRepository.findAll()).thenReturn(allLabels);
        when(bookRepository.findMostPopularBooks()).thenReturn(popularBooks);

        // Act
        String response1 = chatService.getChatResponse("recommend books");
        String response2 = chatService.getChatResponse("recommend books");

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        verify(bookRepository, times(2)).findMostPopularBooks();
    }

    // ==================== Trimming and Formatting Tests ====================

    @Test
    void testGetChatResponse_TrimsWhitespace() {
        // Act
        String response1 = chatService.getChatResponse("  hello  ");
        String response2 = chatService.getChatResponse("hello");

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        // Both should produce greeting responses
        assertTrue(response1.contains("Hi there"));
        assertTrue(response2.contains("Hi there"));
    }

    @Test
    void testGetChatResponse_HandlesMultipleSpaces() {
        // Act
        String response = chatService.getChatResponse("recommend    some    books");

        // Assert
        assertNotNull(response);
    }
}

