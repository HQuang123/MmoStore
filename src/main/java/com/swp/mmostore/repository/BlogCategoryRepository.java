package com.swp.mmostore.repository;

import com.swp.mmostore.entity.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Integer> {
    Optional<BlogCategory> findByName(String name);
}
