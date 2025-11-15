package com.swp.mmostore.service;

import com.swp.mmostore.entity.BlogComment;
import com.swp.mmostore.entity.BlogPost;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.BlogCommentRepository;
import com.swp.mmostore.repository.BlogPostRepository;
import com.swp.mmostore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogCommentService {

    private final BlogCommentRepository commentRepository;
    private final BlogPostRepository postRepository;
    private final UserRepository userRepository;

    // Thêm comment hoặc reply
    @Transactional
    public BlogComment addComment(Integer postId, String content, String userEmail, Integer parentId) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        User user = userRepository.findByEmail(userEmail);
        if (user == null) throw new RuntimeException("User not found");

        BlogComment comment = new BlogComment();
        comment.setBlogPost(post);
        comment.setUser(user);
        comment.setContent(content);

        if (parentId != null) {
            BlogComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParent(parent);
        }

        return commentRepository.save(comment);
    }

    // Chỉnh sửa comment
    @Transactional
    public BlogComment editComment(Integer commentId, String content, String userEmail) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Kiểm tra quyền: chỉ chủ comment mới edit được
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Không có quyền chỉnh sửa bình luận này");
        }

        comment.setContent(content);
        return commentRepository.save(comment);
    }

    // Xóa comment (soft delete)
    @Transactional
    public void deleteComment(Integer commentId, String userEmail) {
        BlogComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        // Kiểm tra quyền: chỉ chủ comment mới xóa được
        if (!comment.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Không có quyền xóa bình luận này");
        }

        comment.setIsActive(false); // soft delete
        commentRepository.save(comment);
    }

    // Lấy comment gốc + reply của bài viết
    @Transactional(readOnly = true)
    public List<BlogComment> getCommentsByPost(Integer postId) {
        BlogPost post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return commentRepository.findByBlogPostAndParentIsNullAndIsActiveTrue(post);
    }

    // Chỉ lấy comment cha
    @Transactional(readOnly = true)
    public List<BlogComment> getParentCommentsByPost(BlogPost post) {
        return commentRepository.findByBlogPostAndParentIsNullAndIsActiveTrue(post);
    }

    // Lấy comment theo ID
    public BlogComment findById(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with ID: " + commentId));
    }
}
