package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Item;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    @Query("""
        SELECT i FROM Item i
        WHERE i.product.productId = :productId
             AND i.order IS NULL
             And i.isDeleted = false
             and i.isSold = false
        """)
    List<Item> findUnsoldItem(@Param("productId") Integer productId, Pageable pageable);
}
