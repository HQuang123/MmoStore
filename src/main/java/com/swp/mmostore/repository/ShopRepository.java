package com.swp.mmostore.repository;

import com.swp.mmostore.dto.ProductDetailDTO;
import com.swp.mmostore.dto.ShopSummaryDTO;
import com.swp.mmostore.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    @Query("""
                select 
                        s.shopId,
                        s.name,
                        CAST(COUNT(p.productId) AS int),
                        COALESCE(AVG(r.ratingPoint), 0)
                from Shop s
                    left join s.products p
                    left join p.ratings r
                where s.shopId = :shopId
                and s.isDeleted = false
                group by s.shopId
            """)
    public ShopSummaryDTO findShopId(@Param("shopId") Integer shopId);
}
