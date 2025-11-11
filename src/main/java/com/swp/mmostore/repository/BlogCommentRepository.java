package com.swp.mmostore.repository;

import com.swp.mmostore.entity.BlogComment;
import com.swp.mmostore.entity.BlogPost;
import com.swp.mmostore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Integer> {

    // Lấy comment gốc (parent = null) của post
    List<BlogComment> findByBlogPostAndParentIsNullAndIsActiveTrue(BlogPost post);

    // Lấy replies của 1 comment
    List<BlogComment> findByParentAndIsActiveTrue(BlogComment parent);
}
