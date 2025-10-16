package com.swp.mmostore.repository;

import com.swp.mmostore.dto.RatingDTO;
import com.swp.mmostore.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    @Query("""
        SELECT r.id, r.ratingPoint, r.feedback, u.name ,r.createAt
        FROM Rating r
        JOIN r.user u
        JOIN r.product p
        WHERE p.productId = :productId
        AND r.isDeleted = false
        ORDER BY r.createAt DESC
    """)
    List<RatingDTO> findAllByProductId(@Param("productId") Integer productId);
}
