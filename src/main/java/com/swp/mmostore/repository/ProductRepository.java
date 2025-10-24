package com.swp.mmostore.repository;

import com.swp.mmostore.dto.ProductDetailDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Category;
import com.swp.mmostore.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p order by p.createAt limit 12")
    public List<Product> getTwelveLastestProduct();

    @Query("select p from Product p join p.category c where c.categoryId in :categoryId")
    public List<Product> findByCategoryId(@Param("categoryId") List<String> categoryId);

    @Query("""
            select p.productId, p.title, p.description, p.price, s.name, COALESCE(AVG(r.ratingPoint), 0) from Product p
                left join p.shop s
                left join p.category c
                left join p.ratings r
                where c.categoryId IN :categoryId
                and p.isDeleted = false
                group by p.productId
            """)
    public List<ProductSummaryDTO> findAllAndFilterProduct(@Param("categoryId") List<String> categoryId, Pageable pageable);

    @Query("""
            select p.productId, p.title, p.description, p.price, s.name, COALESCE(AVG(r.ratingPoint), 0) from Product p
                left join p.shop s
                left join p.category c
                left join p.ratings r
                where p.isDeleted = false
                group by p.productId
            """)
    public List<ProductSummaryDTO> findAllProduct(Pageable pageable);

    @Query("""
                select count(distinct p.productId)
                from Product p
                left join p.category c
                where c.categoryId in :categoryId
                and p.isDeleted = false
            """)
    long countFilteredProducts(@Param("categoryId") List<String> categoryId);

    @Query("""
                select count(distinct p.productId)
                from Product p
                where p.isDeleted = false
            """)
    long countAllProducts();

    @Query("""
                SELECT
                        p.productId,
                        p.title,
                        p.description,
                        p.price,                              
                        s.shopId,
                        COALESCE(AVG(r.ratingPoint), 0),
                        CAST(COUNT(r.id) AS int),
                        CAST(COALESCE(SUM(o.quantity), 0) AS int),
                        c.name
                from Product p
                    left join p.shop s
                    left join p.category c
                    left join p.ratings r
                    left join Order o on o.product = p
                where p.productId = :productId
                and p.isDeleted = false 
                group by p.productId
            """)
    public ProductDetailDTO findProductById(@Param("productId") Integer productId);
    @Modifying
    @Query("UPDATE Product p SET p.isDeleted = :status WHERE p.shop.shopId = :shopId")
    void updateProductDeletedStatusByShopId(@Param("shopId") Integer shopId, @Param("status") Boolean status);


    @Query("""
            SELECT new com.swp.mmostore.dto.ProductSummaryDTO(
                p.productId, p.title, p.description, p.price, s.name, COALESCE(AVG(r.ratingPoint), 0)
            )
            FROM Product p
            LEFT JOIN p.shop s
            LEFT JOIN p.category c
            LEFT JOIN p.ratings r
            WHERE p.isDeleted = false
              AND (:keyword IS NULL OR TRIM(:keyword) = '' 
                   OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (
                   :categoryIds IS NULL 
                   OR COALESCE(:categoryIds, NULL) IS NULL 
                   OR c.categoryId IN :categoryIds
              )
            GROUP BY p.productId, p.title, p.description, p.price, s.name
            """)
    List<ProductSummaryDTO> findProductByTitle(
            @Param("keyword") String keyword,
            @Param("categoryIds") List<String> categoryIds,
            Pageable pageable
    );


    @Query("""
    SELECT COUNT(DISTINCT p.productId)
    FROM Product p
    LEFT JOIN p.category c
    WHERE p.isDeleted = false
      AND (:keyword IS NULL OR TRIM(:keyword) = '' 
           OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (
           :categoryId IS NULL 
           OR COALESCE(:categoryId, NULL) IS NULL 
           OR c.categoryId IN :categoryId
      )
    """)
    long countByKeywordAndCategories(
            @Param("keyword") String keyword,
            @Param("categoryId") List<String> categoryId
    );

    @Query("""
            SELECT new com.swp.mmostore.dto.ProductSummaryDTO(
                p.productId, p.title, p.description, p.price, s.name, COALESCE(AVG(r.ratingPoint), 0)
            )
            FROM Product p
            LEFT JOIN p.shop s
            LEFT JOIN p.ratings r
            WHERE p.isDeleted = false
              AND s.shopId = :shopId
            GROUP BY p.productId, p.title, p.description, p.price, s.name
            """)
    List<ProductSummaryDTO> findProductsByShopId(@Param("shopId") Integer shopId, Pageable pageable);

    @Query("""
            SELECT COUNT(DISTINCT p.productId)
            FROM Product p
            WHERE p.isDeleted = false
              AND p.shop.shopId = :shopId
            """)
    long countProductsByShopId(@Param("shopId") Integer shopId);
}
