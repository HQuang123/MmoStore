package com.swp.mmostore.service;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public Page<Category> findPaginatedAndFiltered(int page, int size, String keyword, Boolean isDeleted) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findFiltered(keyword, isDeleted, pageable);
    }

    public void saveCategory(Category category) {
        if (category.getIsDeleted() == null) category.setIsDeleted(false);
        categoryRepository.save(category);
    }

    public Category findById(Integer id) {
        return categoryRepository.findById(id).orElse(null);
    }

    @Transactional
    public void toggleCategoryStatus(Integer id) {
        categoryRepository.findById(id).ifPresent(category -> {
            category.setIsDeleted(!Boolean.TRUE.equals(category.getIsDeleted()));
            categoryRepository.save(category);
        });
    }
}
