package com.swp.mmostore.service;

import com.swp.mmostore.entity.BlogCategory;
import com.swp.mmostore.entity.BlogPost;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.BlogCategoryRepository;
import com.swp.mmostore.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


@Service
public class BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    // Lấy tất cả bài viết active, phân trang
    public Page<BlogPost> getAllActivePostsPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createAt").descending());
        return blogPostRepository.findByIsActiveTrueOrderByCreateAtDesc(pageable);
    }


    public Page<BlogPost> searchActivePosts(String title, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogPostRepository.searchActivePosts(
                (title == null || title.isBlank()) ? null : title,
                (category == null || category.isBlank()) ? null : category,
                pageable
        );
    }


    // Lưu bài viết
    public BlogPost saveBlogPost(BlogPost blogPost) {
        return blogPostRepository.save(blogPost);
    }

    // Lấy bài viết theo ID
    public BlogPost findById(Integer id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    // Xóa mềm bài viết
    public void softDeleteBlogPost(Integer id) {
        BlogPost post = findById(id);
        if (post != null) {
            post.setIsActive(false);  // set isActive = 0 hoặc false
            blogPostRepository.save(post);
        }
    }
    public Page<BlogPost> findAllByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogPostRepository.findByUserAndIsActiveTrueOrderByCreateAtDesc(user, pageable);
    }
    public Page<BlogPost> searchActivePostsByUser(User user, String title, String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogPostRepository.searchActivePostsByUser(
                user,
                (title == null || title.isBlank()) ? null : title,
                (category == null || category.isBlank()) ? null : category,
                pageable
        );
    }

    public long countActivePosts(String title, String category) {
        return blogPostRepository.countActivePosts(title, category);
    }

    public long countActivePostsByUser(User user, String title, String category) {
        return blogPostRepository.countActivePostsByUser(user, title, category);
    }

    public void save(BlogPost post) {
        blogPostRepository.save(post);
    }


    public BlogPost updatePost(Integer id, BlogPost editedPost) {
        BlogPost existing = findById(id);

        existing.setTitle(editedPost.getTitle());
        existing.setContent(editedPost.getContent());

        if (editedPost.getCategory() != null) {
            existing.setCategory(editedPost.getCategory());
        }

        if (editedPost.getImageUrl() != null) {
            existing.setImageUrl(editedPost.getImageUrl());
        }

        existing.setUpdateAt(LocalDateTime.now());

        return blogPostRepository.save(existing);
    }
    public BlogPost getPostById(int id) {
        return blogPostRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại hoặc đã bị xóa"));
    }
    @Transactional
    public void updatePost(int id, String title, String content, int categoryId) {
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài viết không tồn tại"));

        BlogCategory category = blogCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));

        existingPost.setTitle(title);
        existingPost.setContent(content);
        existingPost.setCategory(category);

        blogPostRepository.save(existingPost);
    }



    // Lấy danh sách bài viết cho admin với filter và phân trang
    public Page<BlogPost> getPostsForAdmin(String title, String category, Integer status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return blogPostRepository.searchForAdmin(
                (title == null || title.isBlank()) ? null : title,
                (category == null || category.isBlank()) ? null : category,
                status,
                pageable
        );
    }

    public void rejectPost(Integer id) {
        BlogPost post = getPostById(id);
        if (post != null) {
            post.setStatus(-1); // từ chối
            blogPostRepository.save(post);
        }
    }
    // Duyệt bài viết
    public void approvePost(int id) {
        blogPostRepository.findById(id).ifPresent(post -> {
            post.setStatus(1); // 1 = approved
            blogPostRepository.save(post);
        });
    }

    // Xóa bài viết
    public void deletePost(int id) {
        BlogPost post = blogPostRepository.findById(id).orElse(null);
        if (post != null) {
            post.setIsActive(true);  // đánh dấu xóa mềm
            blogPostRepository.save(post);
        }
    }



}
