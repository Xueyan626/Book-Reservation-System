package usyd.library_reservation_system.library_reservation_system.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO;
import usyd.library_reservation_system.library_reservation_system.dto.CommentDTO;
import usyd.library_reservation_system.library_reservation_system.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private ObjectMapper objectMapper;

    private CommentDTO mockCommentDTO;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
        objectMapper = new ObjectMapper();

        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        mockCommentDTO = new CommentDTO();
        mockCommentDTO.setCommentId(1);
        mockCommentDTO.setBookId(100);
        mockCommentDTO.setAuthorId(5);
        mockCommentDTO.setAuthorType((byte) 0);
        mockCommentDTO.setContent("Great book!");
        mockCommentDTO.setStatus((byte) 1);
        mockCommentDTO.setAuthorName("Alice");
        mockCommentDTO.setCreateDate(LocalDateTime.now());
    }

    // region addComment()
    @Test
    void testAddComment_ShouldReturnCreatedComment() throws Exception {
        when(commentService.addComment(any(CommentDTO.class), anyInt()))
                .thenReturn(mockCommentDTO);

        mockMvc.perform(post("/api/comments/book/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCommentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Great book!"))
                .andExpect(jsonPath("$.authorName").value("Alice"))
                .andExpect(jsonPath("$.bookId").value(100));

        verify(commentService).addComment(any(CommentDTO.class), eq(100));
    }
    // endregion

    // region getCommentsByBookId()
    @Test
    void testGetCommentsByBookId_ShouldReturnList() throws Exception {
        when(commentService.getCommentsByBookId(100))
                .thenReturn(List.of(mockCommentDTO));

        mockMvc.perform(get("/api/comments/book/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great book!"))
                .andExpect(jsonPath("$[0].authorName").value("Alice"));

        verify(commentService).getCommentsByBookId(100);
    }
    // endregion

    // region getAllCommentsByBookId()
    @Test
    void testGetAllCommentsByBookId_Admin_ShouldReturnList() throws Exception {
        when(commentService.getAllCommentsByBookId(100, (byte) 1))
                .thenReturn(List.of(mockCommentDTO));

        mockMvc.perform(get("/api/comments/book/100/all")
                        .param("authorType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Great book!"));

        verify(commentService).getAllCommentsByBookId(100, (byte) 1);
    }
    // endregion

    // region hideComment()
    @Test
    void testHideComment_ShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).hideComment(1, (byte) 1);

        mockMvc.perform(put("/api/comments/hide/1")
                        .param("authorType", "1"))
                .andExpect(status().isNoContent());

        verify(commentService).hideComment(1, (byte) 1);
    }

    @Test
    void testHideComment_NotAdmin_ShouldThrowException() {
        doThrow(new RuntimeException("No permission to hide comments"))
                .when(commentService).hideComment(1, (byte) 0);

        assertThrows(Exception.class, () -> {
            mockMvc.perform(put("/api/comments/hide/1")
                    .param("authorType", "0"));
        });

        verify(commentService).hideComment(1, (byte) 0);
    }

    // endregion

    // region restoreComment()
    @Test
    void testRestoreComment_ShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).restoreComment(1, (byte) 1);

        mockMvc.perform(put("/api/comments/restore/1")
                        .param("authorType", "1"))
                .andExpect(status().isNoContent());

        verify(commentService).restoreComment(1, (byte) 1);
    }
    // endregion

    // region getAllCommentsForAdmin()
    @Test
    void testGetAllCommentsForAdmin_ShouldReturnList() throws Exception {
        AdminCommentViewDTO adminView = new AdminCommentViewDTO(
                1, "Book A", "Alice", "Nice book!",
                LocalDateTime.now(), (byte) 1
        );

        when(commentService.getAllCommentsForAdmin((byte) 1))
                .thenReturn(List.of(adminView));

        mockMvc.perform(get("/api/comments/admin/all")
                        .param("authorType", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookName").value("Book A"))
                .andExpect(jsonPath("$[0].userName").value("Alice"));

        verify(commentService).getAllCommentsForAdmin((byte) 1);
    }
    // endregion
}
