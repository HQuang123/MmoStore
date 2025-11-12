package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    @Query("SELECT c FROM Category c " +
            "WHERE (:keyword IS NULL OR c.name LIKE %:keyword%) " +
            "AND (:isDeleted IS NULL OR c.isDeleted = :isDeleted)")
    Page<Category> findFiltered(@Param("keyword") String keyword,
                                @Param("isDeleted") Boolean isDeleted,
                                Pageable pageable);

    Optional<Category> findByName(String categoryName);
}
