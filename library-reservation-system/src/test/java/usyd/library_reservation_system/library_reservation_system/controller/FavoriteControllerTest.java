package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.model.Book;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.model.favorite.Favorite;
import usyd.library_reservation_system.library_reservation_system.model.favorite.FavoriteId;
import usyd.library_reservation_system.library_reservation_system.repository.BookRepository;
import usyd.library_reservation_system.library_reservation_system.repository.FavoriteRepository;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    private Book testBook;
    private UserEntity testUser;
    private Integer testUserId;
    private Integer testBookId;

    @BeforeEach
    void setUp() {
        // Create a test user
        long timestamp = System.currentTimeMillis();
        testUser = new UserEntity();
        testUser.setNickname("testuser_" + timestamp);
        testUser.setEmail("test_" + timestamp + "@example.com");
        testUser.setTelephone("1234567" + (timestamp % 10000));
        testUser.setPasswordHash("$2a$10$hashedpassword");
        testUser.setIsActive(true);
        testUser = userRepository.save(testUser);
        testUserId = testUser.getUserId();

        // Create a test book
        testBook = new Book();
        testBook.setBookName("Test Book for Favorites");
        testBook.setAuthor("Test Author");
        testBook.setQuantity(10);
        testBook.setNumFavorite(0);
        testBook.setNumReservation(0);
        testBook.setLabelId(1);
        testBook.setAvatar("/img/test.jpg");
        testBook = bookRepository.save(testBook);
        testBookId = testBook.getBookId();
    }

    @Test
    void testToggle_AddFavorite() throws Exception {
        Map<String, Integer> requestBody = Map.of("bookId", testBookId);

        mockMvc.perform(post("/api/favorites/toggle")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true))
                .andExpect(jsonPath("$.bookId").value(testBookId))
                .andExpect(jsonPath("$.userId").value(testUserId));
    }

    @Test
    void testToggle_RemoveFavorite() throws Exception {
        // First add a favorite
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        Map<String, Integer> requestBody = Map.of("bookId", testBookId);

        // Toggle to remove
        mockMvc.perform(post("/api/favorites/toggle")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(false))
                .andExpect(jsonPath("$.bookId").value(testBookId))
                .andExpect(jsonPath("$.userId").value(testUserId));
    }

    @Test
    void testToggle_MissingHeader() throws Exception {
        Map<String, Integer> requestBody = Map.of("bookId", testBookId);

        mockMvc.perform(post("/api/favorites/toggle")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Missing X-USER-ID header"));
    }

    @Test
    void testToggle_InvalidHeader() throws Exception {
        Map<String, Integer> requestBody = Map.of("bookId", testBookId);

        mockMvc.perform(post("/api/favorites/toggle")
                        .header("X-USER-ID", "invalid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testToggle_MissingBookId() throws Exception {
        Map<String, Object> requestBody = Map.of();

        mockMvc.perform(post("/api/favorites/toggle")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("bookId is required"));
    }

    @Test
    void testCheck_Favorited() throws Exception {
        // First add a favorite
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        mockMvc.perform(get("/api/favorites/" + testBookId + "/check")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(true));
    }

    @Test
    void testCheck_NotFavorited() throws Exception {
        mockMvc.perform(get("/api/favorites/" + testBookId + "/check")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(false));
    }

    @Test
    void testRemove_ExistingFavorite() throws Exception {
        // First add a favorite
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        mockMvc.perform(delete("/api/favorites/" + testBookId)
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemove_NonExistingFavorite() throws Exception {
        mockMvc.perform(delete("/api/favorites/" + testBookId)
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testMyFavorites_Empty() throws Exception {
        mockMvc.perform(get("/api/favorites")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testMyFavorites_WithFavorites() throws Exception {
        // Add some favorites
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        mockMvc.perform(get("/api/favorites")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].bookId").value(testBookId))
                .andExpect(jsonPath("$[0].bookName").value("Test Book for Favorites"))
                .andExpect(jsonPath("$[0].author").value("Test Author"));
    }

    @Test
    void testRemoveBatch_SingleBook() throws Exception {
        // Add a favorite
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        Map<String, List<Integer>> requestBody = Map.of("bookIds", List.of(testBookId));

        mockMvc.perform(delete("/api/favorites/batch")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveBatch_MultipleBooks() throws Exception {
        // Create another book
        Book anotherBook = new Book();
        anotherBook.setBookName("Another Test Book");
        anotherBook.setAuthor("Another Author");
        anotherBook.setQuantity(5);
        anotherBook.setNumFavorite(0);
        anotherBook.setNumReservation(0);
        anotherBook.setLabelId(1);
        anotherBook = bookRepository.save(anotherBook);

        // Add favorites
        FavoriteId favoriteId1 = new FavoriteId(testBookId, testUserId);
        Favorite favorite1 = Favorite.builder()
                .id(favoriteId1)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite1);

        FavoriteId favoriteId2 = new FavoriteId(anotherBook.getBookId(), testUserId);
        Favorite favorite2 = Favorite.builder()
                .id(favoriteId2)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite2);

        Map<String, List<Integer>> requestBody = Map.of(
                "bookIds", List.of(testBookId, anotherBook.getBookId())
        );

        mockMvc.perform(delete("/api/favorites/batch")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testRemoveBatch_EmptyList() throws Exception {
        Map<String, List<Integer>> requestBody = Map.of("bookIds", List.of());

        mockMvc.perform(delete("/api/favorites/batch")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent());
    }

    @Test
    void testIncrementFavoriteCount_Success() throws Exception {
        mockMvc.perform(post("/api/favorites/" + testBookId + "/favorite")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testIncrementFavoriteCount_AlreadyFavorited() throws Exception {
        // Add a favorite first
        FavoriteId favoriteId = new FavoriteId(testBookId, testUserId);
        Favorite favorite = Favorite.builder()
                .id(favoriteId)
                .createTime(LocalDateTime.now())
                .build();
        favoriteRepository.save(favorite);

        // Try to add again (should still succeed as the service handles duplicates)
        mockMvc.perform(post("/api/favorites/" + testBookId + "/favorite")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testIncrementFavoriteCount_MissingHeader() throws Exception {
        mockMvc.perform(post("/api/favorites/" + testBookId + "/favorite"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Missing X-USER-ID header"));
    }

    @Test
    void testIncrementFavoriteCount_InvalidHeader() throws Exception {
        mockMvc.perform(post("/api/favorites/" + testBookId + "/favorite")
                        .header("X-USER-ID", "invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testIncrementFavoriteCount_InvalidBookId() throws Exception {
        // When book doesn't exist, service doesn't throw exception, just returns normally
        mockMvc.perform(post("/api/favorites/999999/favorite")
                        .header("X-USER-ID", testUserId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void testCheck_NullUserId() throws Exception {
        // If no header is provided, userId will be null
        mockMvc.perform(get("/api/favorites/" + testBookId + "/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.favorited").value(false));
    }

    @Test
    void testRemove_NullUserId() throws Exception {
        // If no header is provided, userId will be null
        mockMvc.perform(delete("/api/favorites/" + testBookId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testMyFavorites_NullUserId() throws Exception {
        // If no header is provided, userId will be null
        mockMvc.perform(get("/api/favorites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testRemoveBatch_NoBookIdsKey() throws Exception {
        Map<String, Object> requestBody = Map.of();

        mockMvc.perform(delete("/api/favorites/batch")
                        .header("X-USER-ID", testUserId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNoContent());
    }
}

