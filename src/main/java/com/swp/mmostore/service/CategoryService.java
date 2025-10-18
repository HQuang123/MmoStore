package com.swp.mmostore.service;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.repository.CategoryRepository;
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

    public void toggleCategoryStatus(Integer categoryId) {
        categoryRepository.findById(categoryId).ifPresent(category -> {
            category.setIsDeleted(!category.getIsDeleted());
            categoryRepository.save(category);
        });
    }
}
