package com.swp.mmostore.service;

import com.swp.mmostore.entity.BlogCategory;
import com.swp.mmostore.repository.BlogCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogCategoryService {

    @Autowired
    private BlogCategoryRepository blogCategoryRepository;

    // Lấy tất cả category
    public List<BlogCategory> getAllCategories() {
        return blogCategoryRepository.findAll();
    }

    // Lấy category theo tên
    public BlogCategory getCategoryByName(String name) {
        return blogCategoryRepository.findByName(name).orElse(null);
    }
    // Lấy category theo ID
    public BlogCategory getCategoryById(int id) {
        return blogCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category không tồn tại với ID: " + id));
    }


}
