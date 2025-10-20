package com.swp.mmostore.service;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CloudStorageService cloudStorageService;

    public Page<Category> findPaginatedAndFiltered(int page, int size, String keyword, Boolean isDeleted) {
        Pageable pageable = PageRequest.of(page, size);
        return categoryRepository.findFiltered(keyword, isDeleted, pageable);
    }

    public void saveCategory(Category category, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = cloudStorageService.uploadFile(imageFile);
            category.setCategoryImageUrl(imageUrl);
        } else if (category.getCategoryId() != null) {
            // Keep existing image if not replaced
            Category existing = categoryRepository.findById(category.getCategoryId()).orElse(null);
            if (existing != null) {
                category.setCategoryImageUrl(existing.getCategoryImageUrl());
            }
        }

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
