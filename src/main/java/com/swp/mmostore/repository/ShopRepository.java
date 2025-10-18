package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
    @Query("""
            SELECT s FROM Shop s
            WHERE (:keyword IS NULL OR :keyword = '' 
                   OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(s.user.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:isDeleted IS NULL OR s.isDeleted = :isDeleted)
            """)
    Page<Shop> findFiltered(
            @Param("keyword") String keyword,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable);

}
