package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import usyd.library_reservation_system.library_reservation_system.model.Book;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBookById_Exists() throws Exception {
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void testGetBookById_NotExists() throws Exception {
        mockMvc.perform(get("/api/books/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateBook() throws Exception {
        Book book = new Book();
        book.setBookName("Test Book");
        book.setAuthor("Test Author");
        book.setDescription("Test Description");
        book.setQuantity(10);
        book.setLabelId(1);
        book.setAvatar("/img/test.jpg");

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookName").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void testUpdateBook_Exists() throws Exception {
        Book book = new Book();
        book.setBookName("Updated Book Name");
        book.setAuthor("Updated Author");
        book.setDescription("Updated Description");
        book.setQuantity(15);
        book.setLabelId(1);
        book.setAvatar("/img/updated.jpg");

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void testUpdateBook_NotExists() throws Exception {
        Book book = new Book();
        book.setBookName("Non-existent Book");
        book.setAuthor("Non-existent Author");
        book.setQuantity(10);
        book.setLabelId(1);

        mockMvc.perform(put("/api/books/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteBook_NotExists() throws Exception {
        mockMvc.perform(delete("/api/books/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchBooks() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "Python"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSearchBooksByName() throws Exception {
        mockMvc.perform(get("/api/books/search/name")
                        .param("name", "Python"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testSearchBooksByAuthor() throws Exception {
        mockMvc.perform(get("/api/books/search/author")
                        .param("author", "Zhang"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBooksByLabelId() throws Exception {
        mockMvc.perform(get("/api/books/label/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetAvailableBooks() throws Exception {
        mockMvc.perform(get("/api/books/available"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetMostPopularBooks() throws Exception {
        mockMvc.perform(get("/api/books/popular"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetMostReservedBooks() throws Exception {
        mockMvc.perform(get("/api/books/most-reserved"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUpdateBookQuantity_Exists() throws Exception {
        String quantityRequest = "{\"quantity\": 25}";

        mockMvc.perform(patch("/api/books/1/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quantityRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void testUpdateBookQuantity_NotExists() throws Exception {
        String quantityRequest = "{\"quantity\": 25}";

        mockMvc.perform(patch("/api/books/99999/quantity")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(quantityRequest))
                .andExpect(status().isNotFound());
    }

    @Test
    void testIncrementFavoriteCount_WithValidHeader() throws Exception {
        mockMvc.perform(post("/api/books/1/favorite")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testIncrementFavoriteCount_WithoutHeader() throws Exception {
        mockMvc.perform(post("/api/books/1/favorite"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testIncrementFavoriteCount_WithInvalidHeader() throws Exception {
        mockMvc.perform(post("/api/books/1/favorite")
                        .header("X-USER-ID", "invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void testIncrementReservationCount_Exists() throws Exception {
        mockMvc.perform(post("/api/books/1/reservation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void testIncrementReservationCount_NotExists() throws Exception {
        mockMvc.perform(post("/api/books/99999/reservation"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testSearchByLabel() throws Exception {
        mockMvc.perform(get("/api/books/search-by-label")
                        .param("q", "Programming"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testUploadCover_WithValidFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-cover.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/books/cover").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value("true"))
                .andExpect(jsonPath("$.filename").exists());
    }

    @Test
    void testUploadCover_WithEmptyFile() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        mockMvc.perform(multipart("/api/books/cover").file(emptyFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("No file uploaded"));
    }

    @Test
    void testCreateBook_WithMinimalFields() throws Exception {
        Book book = new Book();
        book.setBookName("Minimal Field Test Book");
        book.setAuthor("Minimal Author");
        book.setLabelId(1);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookName").value("Minimal Field Test Book"));
    }

    @Test
    void testSearchBooks_EmptyResult() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "NonexistentKeywordxyzabc123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void testGetBooksByLabelId_InvalidLabel() throws Exception {
        mockMvc.perform(get("/api/books/label/99999"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

