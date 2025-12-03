package usyd.library_reservation_system.library_reservation_system.service;

import usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO;
import usyd.library_reservation_system.library_reservation_system.dto.CommentDTO;
import usyd.library_reservation_system.library_reservation_system.model.Comment;
import usyd.library_reservation_system.library_reservation_system.repository.CommentRepository;
import usyd.library_reservation_system.library_reservation_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    // Add comment
    public CommentDTO addComment(CommentDTO dto, Integer bookId) {
        Comment comment = new Comment();
        comment.setBookId(bookId);
        comment.setContent(dto.getContent());
        comment.setAuthorId(dto.getAuthorId());
        comment.setAuthorType(dto.getAuthorType());
        comment.setStatus((byte) 1); // Default visible

        Comment saved = commentRepository.save(comment);

        dto.setCommentId(saved.getCommentId());
        dto.setStatus(saved.getStatus());
        dto.setCreateDate(saved.getCreateDate());
        return dto;
    }

    // Get unblocked comments for a book
    public List<CommentDTO> getCommentsByBookId(Integer bookId) {
        return commentRepository.findByBookIdAndStatus(bookId, (byte) 1)
                .stream()
                .map(c -> {
                    CommentDTO dto = new CommentDTO();
                    dto.setCommentId(c.getCommentId());
                    dto.setBookId(c.getBookId());
                    dto.setContent(c.getContent());
                    dto.setAuthorId(c.getAuthorId());
                    dto.setAuthorType(c.getAuthorType());
                    dto.setStatus(c.getStatus());
                    dto.setCreateDate(c.getCreateDate());
                    
                    // Query username
                    if (c.getAuthorId() != null) {
                        userRepository.findById(c.getAuthorId())
                                .ifPresent(user -> dto.setAuthorName(user.getNickname()));
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Admin: View all comments (including blocked ones)
    public List<CommentDTO> getAllCommentsByBookId(Integer bookId, Byte authorType) {
        if (authorType != 1) {
            throw new RuntimeException("No permission to view all comments");
        }

        return commentRepository.findByBookId(bookId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Hide comment (admin only)
    public void hideComment(Integer commentId, Byte authorType) {
        if (authorType != 1) {
            throw new RuntimeException("No permission to hide comments");
        }
        commentRepository.softDeleteComment(commentId);
    }

    // Restore comment (admin only)
    public void restoreComment(Integer commentId, Byte authorType) {
        if (authorType != 1) {
            throw new RuntimeException("No permission to restore comments");
        }
        commentRepository.restoreComment(commentId);
    }

    public List<AdminCommentViewDTO> getAllCommentsForAdmin(Byte authorType) {
        if (authorType != 1) {
            throw new RuntimeException("No permission to view all comments");
        }
        return commentRepository.findAllCommentsWithBookAndUser();
    }


    // DTO conversion utility method
    private CommentDTO convertToDTO(Comment c) {
        CommentDTO dto = new CommentDTO();
        dto.setCommentId(c.getCommentId());
        dto.setBookId(c.getBookId());
        dto.setContent(c.getContent());
        dto.setAuthorId(c.getAuthorId());
        dto.setAuthorType(c.getAuthorType());
        dto.setStatus(c.getStatus());
        dto.setCreateDate(c.getCreateDate());
        return dto;
    }
}
