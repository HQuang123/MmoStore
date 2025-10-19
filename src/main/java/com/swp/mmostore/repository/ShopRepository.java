package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {
    @Query("""
        SELECT s.shopId FROM Shop s
        WHERE (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:isDeleted IS NULL OR s.isDeleted = :isDeleted)
        """)
    Page<Integer> findShopIdsFiltered(@Param("keyword") String keyword,
                                      @Param("isDeleted") Boolean isDeleted,
                                      Pageable pageable);

    @Query("""
        SELECT s FROM Shop s
        LEFT JOIN FETCH s.user
        WHERE s.shopId IN :ids
        """)
    List<Shop> findAllByIdWithUser(@Param("ids") List<Integer> ids);
}
