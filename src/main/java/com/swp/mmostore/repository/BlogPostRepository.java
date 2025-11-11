package com.swp.mmostore.repository;

import com.swp.mmostore.entity.BlogCategory;
import com.swp.mmostore.entity.BlogPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.swp.mmostore.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Integer> {

    // Lấy tất cả bài active, sắp xếp theo createAt desc
    Page<BlogPost> findByIsActiveTrueOrderByCreateAtDesc(Pageable pageable);

    @Query("""
    SELECT p FROM BlogPost p
    WHERE p.isActive = true AND p.status= 1
      AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:category IS NULL OR LOWER(p.category.name) = LOWER(:category))
    ORDER BY p.createAt DESC
""")
    Page<BlogPost> searchActivePosts(
            @Param("title") String title,
            @Param("category") String category,
            Pageable pageable
    );


    // Lấy bài viết của user theo trang (active only)
    Page<BlogPost> findByUserAndIsActiveTrueOrderByCreateAtDesc(User user, Pageable pageable);


    @Query("""
    SELECT p FROM BlogPost p
    WHERE p.isActive = true
      AND p.user = :user
      AND (:title IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:category IS NULL OR LOWER(p.category.name) = LOWER(:category))
    ORDER BY p.createAt DESC
""")
    Page<BlogPost> searchActivePostsByUser(
            @Param("user") User user,
            @Param("title") String title,
            @Param("category") String category,
            Pageable pageable
    );

    @Query("SELECT COUNT(bp) FROM BlogPost bp " +
            "WHERE bp.isActive = true " +
            "AND (:title IS NULL OR LOWER(bp.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:category IS NULL OR bp.category.name = :category)")
    long countActivePosts(@Param("title") String title, @Param("category") String category);


    // Đếm tất cả bài viết của người dùng theo title và category, chỉ những bài viết active
    @Query("SELECT COUNT(bp) FROM BlogPost bp WHERE bp.isActive = true AND bp.user = :user " +
            "AND (:title IS NULL OR LOWER(bp.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:category IS NULL OR bp.category.name = :category)")
    long countActivePostsByUser(@Param("user") User user, @Param("title") String title,
                                @Param("category") String category);
    Optional<BlogPost> findByIdAndIsActiveTrue(Integer id);

    @Query("""
    SELECT b FROM BlogPost b
    WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')))
      AND (:category IS NULL OR b.category.name = :category)
      AND (:status IS NULL OR b.status = :status)
    ORDER BY b.createAt DESC
""")
    Page<BlogPost> searchForAdmin(
            @Param("title") String title,
            @Param("category") String category,
            @Param("status") Integer status,
            Pageable pageable
    );
}
