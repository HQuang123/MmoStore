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
import java.util.Optional;

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

    public void saveCategory(Category category, MultipartFile imageFile) throws IOException, IllegalArgumentException {
        Optional<Category> existingByName = categoryRepository.findByName(category.getName());

        if (existingByName.isPresent()) {
            // A category with this name exists. Now check if it's the *same* category
            // we are trying to update or a *different* one.

            // This is an "update" operation and the found category is NOT the same one.
            if (category.getCategoryId() != null && !existingByName.get().getCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("Another category with the name '" + category.getName() + "' already exists.");
            }

            // This is an "add new" operation (ID is null) but the name is already taken.
            if (category.getCategoryId() == null) {
                throw new IllegalArgumentException("A category with the name '" + category.getName() + "' already exists.");
            }
        }

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
