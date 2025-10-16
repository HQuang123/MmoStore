package com.swp.mmostore.repository;

import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Category;
import com.swp.mmostore.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
