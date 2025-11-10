package com.swp.mmostore.repository;

import com.swp.mmostore.dto.ProductDetailDTO;
import com.swp.mmostore.dto.ShopSummaryDTO;
import com.swp.mmostore.dto.ShopViewDTO;
import com.swp.mmostore.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    @Query("""
                select
                        s.shopId,
                        s.name,
                        s.shopImageUrl,
                        CAST(COUNT(p.productId) AS int),
                        COALESCE(AVG(r.ratingPoint), 0),
                        s.shopImageUrl
                from Shop s
                    left join s.products p
                    left join p.ratings r
                where s.shopId = :shopId
                and s.isDeleted = false
                group by s.shopId
            """)
    public ShopSummaryDTO findShopId(@Param("shopId") Integer shopId);
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

    Shop findByUser_UserId(Integer userId);

    @Query("""
        SELECT new com.swp.mmostore.dto.ShopViewDTO(
            s.shopId,
            s.name,
            s.description,
            s.shopImageUrl,
            s.user.name,
            s.user.email,
            COUNT(DISTINCT p.productId),
            COALESCE(AVG(r.ratingPoint), 0.0),
            s.createAt,
            s.updateAt
        )
        FROM Shop s
        LEFT JOIN s.products p
        LEFT JOIN p.ratings r
        WHERE s.shopId = :shopId
          AND s.isDeleted = false
        GROUP BY s.shopId, s.name, s.description, s.shopImageUrl, s.user.name, s.createAt, s.updateAt
        """)
    ShopViewDTO findShopViewById(@Param("shopId") Integer shopId);


}
