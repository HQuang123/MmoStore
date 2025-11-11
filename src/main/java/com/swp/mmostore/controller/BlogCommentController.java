package com.swp.mmostore.controller;

import com.swp.mmostore.entity.BlogComment;
import com.swp.mmostore.service.BlogCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog/comments")
@RequiredArgsConstructor
public class BlogCommentController {

    private final BlogCommentService commentService;


    @PostMapping("/blog/comment")
    public String addComment(
            @RequestParam Integer postId,
            @RequestParam String content,
            @RequestParam(required = false) Integer parentId,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            // Chưa login, có thể redirect sang login hoặc trả về lỗi
            return "redirect:/login";
        }

        String userEmail = auth.getName(); // Lấy email user
        commentService.addComment(postId, content, userEmail, parentId);

        return "redirect:/blog/blog-view";
    }


    // Xóa comment (sử dụng POST thay vì DELETE)
    @PostMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Integer commentId,
                                                Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(403).body("Unauthorized");
        }

        String userEmail = auth.getName();
        commentService.deleteComment(commentId, userEmail);
        return ResponseEntity.ok("Comment deleted");
    }


    // Lấy tất cả comment (với replies) của bài viết
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<BlogComment>> getCommentsByPost(@PathVariable Integer postId) {
        List<BlogComment> comments = commentService.getCommentsByPost(postId);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/edit/{commentId}")
    public String editComment(
            @PathVariable Integer commentId,
            @RequestParam String content,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        String userEmail = auth.getName();

        try {
            commentService.editComment(commentId, content, userEmail);
            // Sau khi edit xong, quay lại trang my-posts
            return "redirect:/blog/my-posts";
        } catch (RuntimeException e) {
            // Có thể thêm flash attribute để hiển thị lỗi nếu muốn
            return "redirect:/blog/my-posts?error=" + e.getMessage();
        } catch (Exception e) {
            return "redirect:/blog/my-posts?error=Error updating comment";
        }
    }



}
