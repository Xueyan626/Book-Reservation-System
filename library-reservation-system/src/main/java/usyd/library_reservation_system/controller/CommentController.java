package usyd.library_reservation_system.library_reservation_system.controller;

import org.springframework.http.ResponseEntity;
import usyd.library_reservation_system.library_reservation_system.dto.AdminCommentViewDTO;
import usyd.library_reservation_system.library_reservation_system.dto.CommentDTO;
import usyd.library_reservation_system.library_reservation_system.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // 添加评论
    @PostMapping("/book/{bookId}")
    public CommentDTO addComment(@RequestBody CommentDTO dto, @PathVariable Integer bookId) {
        return commentService.addComment(dto, bookId);
    }

    // 查询某本书的评论（仅显示status=1）
    @GetMapping("/book/{bookId}")
    public List<CommentDTO> getCommentsByBookId(@PathVariable Integer bookId) {
        return commentService.getCommentsByBookId(bookId);
    }

    // 管理员查看所有评论
    @GetMapping("/book/{bookId}/all")
    public List<CommentDTO> getAllCommentsByBookId(@PathVariable Integer bookId,
                                                   @RequestParam Byte authorType) {
        return commentService.getAllCommentsByBookId(bookId, authorType);
    }

    // 屏蔽评论（仅管理员）
    @PutMapping("/hide/{commentId}")
    public ResponseEntity<String> hideComment(@PathVariable Integer commentId,
                                              @RequestParam Byte authorType) {
        commentService.hideComment(commentId, authorType);
        return ResponseEntity.noContent().build();
    }

    // 恢复评论（仅管理员）
    @PutMapping("/restore/{commentId}")
    public ResponseEntity<String> restoreComment(@PathVariable Integer commentId,
                                                 @RequestParam Byte authorType) {
        commentService.restoreComment(commentId, authorType);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    public List<AdminCommentViewDTO> getAllCommentsForAdmin(@RequestParam Byte authorType) {
        return commentService.getAllCommentsForAdmin(authorType);
    }

}

