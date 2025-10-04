package com.swp.mmostore.repository;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    @Query("SELECT p FROM Product p order by p.createAt limit 12")
    public List<Product> getTwelveLastestProduct();
}
