package usyd.library_reservation_system.library_reservation_system.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO;
import usyd.library_reservation_system.library_reservation_system.dto.CommentDTO;
import usyd.library_reservation_system.library_reservation_system.model.Comment;
import usyd.library_reservation_system.library_reservation_system.model.UserEntity;
import usyd.library_reservation_system.library_reservation_system.repository.CommentRepository;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Comment mockComment;
    private UserEntity mockUser;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        mockComment = new Comment();
        mockComment.setCommentId(1);
        mockComment.setBookId(100);
        mockComment.setContent("Great book!");
        mockComment.setAuthorId(5);
        mockComment.setAuthorType((byte) 0);
        mockComment.setStatus((byte) 1);
        mockComment.setCreateDate(LocalDateTime.now());

        mockUser = new UserEntity();
        mockUser.setUserId(5);
        mockUser.setNickname("Alice");
    }

    // region addComment()
    @Test
    void testAddComment_ShouldSaveSuccessfully() {
        when(commentRepository.save(any(Comment.class))).thenReturn(mockComment);

        CommentDTO dto = new CommentDTO();
        dto.setAuthorId(5);
        dto.setContent("Nice book!");
        dto.setAuthorType((byte) 0);

        CommentDTO result = commentService.addComment(dto, 100);

        assertEquals(1, result.getCommentId());
        assertEquals((byte) 1, result.getStatus());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
    // endregion

    // region getCommentsByBookId()
    @Test
    void testGetCommentsByBookId_WithUserFound_ShouldReturnList() {
        when(commentRepository.findByBookIdAndStatus(100, (byte) 1))
                .thenReturn(List.of(mockComment));
        when(userRepository.findById(5)).thenReturn(Optional.of(mockUser));

        List<CommentDTO> result = commentService.getCommentsByBookId(100);

        assertEquals(1, result.size());
        assertEquals("Great book!", result.get(0).getContent());
        assertEquals("Alice", result.get(0).getAuthorName());
    }

    @Test
    void testGetCommentsByBookId_UserNotFound_ShouldStillReturn() {
        when(commentRepository.findByBookIdAndStatus(100, (byte) 1))
                .thenReturn(List.of(mockComment));
        when(userRepository.findById(5)).thenReturn(Optional.empty());

        List<CommentDTO> result = commentService.getCommentsByBookId(100);

        assertEquals(1, result.size());
        assertNull(result.get(0).getAuthorName());
    }
    // endregion

    // region getAllCommentsByBookId()
    @Test
    void testGetAllCommentsByBookId_Admin_ShouldReturnAll() {
        when(commentRepository.findByBookId(100)).thenReturn(List.of(mockComment));

        List<CommentDTO> result = commentService.getAllCommentsByBookId(100, (byte) 1);

        assertEquals(1, result.size());
        assertEquals("Great book!", result.get(0).getContent());
    }

    @Test
    void testGetAllCommentsByBookId_NotAdmin_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> commentService.getAllCommentsByBookId(100, (byte) 0));
    }
    // endregion

    // region hideComment()
    @Test
    void testHideComment_Admin_ShouldCallRepository() {
        commentService.hideComment(1, (byte) 1);
        verify(commentRepository, times(1)).softDeleteComment(1);
    }

    @Test
    void testHideComment_NotAdmin_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> commentService.hideComment(1, (byte) 0));
        verify(commentRepository, never()).softDeleteComment(anyInt());
    }
    // endregion

    // region restoreComment()
    @Test
    void testRestoreComment_Admin_ShouldCallRepository() {
        commentService.restoreComment(1, (byte) 1);
        verify(commentRepository, times(1)).restoreComment(1);
    }

    @Test
    void testRestoreComment_NotAdmin_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> commentService.restoreComment(1, (byte) 0));
        verify(commentRepository, never()).restoreComment(anyInt());
    }
    // endregion

    // region getAllCommentsForAdmin()
    @Test
    void testGetAllCommentsForAdmin_Admin_ShouldReturnList() {
        AdminCommentViewDTO dto = new AdminCommentViewDTO(
                1, "Book A", "Alice", "Great book!",
                LocalDateTime.now(), (byte) 1
        );

        when(commentRepository.findAllCommentsWithBookAndUser())
                .thenReturn(List.of(dto));

        List<AdminCommentViewDTO> result = commentService.getAllCommentsForAdmin((byte) 1);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getUserName());
    }

    @Test
    void testGetAllCommentsForAdmin_NotAdmin_ShouldThrow() {
        assertThrows(RuntimeException.class,
                () -> commentService.getAllCommentsForAdmin((byte) 0));
    }
    // endregion
}
