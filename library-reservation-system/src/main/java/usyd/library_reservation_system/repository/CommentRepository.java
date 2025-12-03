package usyd.library_reservation_system.library_reservation_system.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO;
import usyd.library_reservation_system.library_reservation_system.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBookIdAndStatus(Integer bookId, Byte status);
    List<Comment> findByBookId(Integer bookId);
    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.status = 0 WHERE c.commentId = :commentId")
    void softDeleteComment(Integer commentId);

    @Transactional
    @Modifying
    @Query("UPDATE Comment c SET c.status = 1 WHERE c.commentId = :commentId")
    void restoreComment(Integer commentId);

    @Query("""
        SELECT new usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO(
            c.commentId,
            b.bookName,
            u.nickname,
            c.content,
            c.createDate,
            c.status
        )
        FROM Comment c
        JOIN Book b ON c.bookId = b.bookId
        JOIN UserEntity u ON c.authorId = u.userId
        """)
    List<AdminCommentViewDTO> findAllCommentsWithBookAndUser();


}

