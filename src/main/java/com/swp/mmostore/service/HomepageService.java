package com.swp.mmostore.service;

import com.swp.mmostore.entity.Category;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.repository.CategoryRepository;
import com.swp.mmostore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomepageService {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public List<Category> getTopSixCategories() {
        List<Category> listFull = categoryRepository.findTop6ByIsDeletedFalseOrderByNameAsc();
        if(listFull.isEmpty()){
            return null;
        }
        return listFull;
    }

    public List<Product> getTopTwelveProducts() {
        return productRepository.getTwelveLastestProduct();
    }
}
