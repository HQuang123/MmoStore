package com.swp.mmostore.service;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> loadAllProduct() {
        return productRepository.findAll();
    }

    public List<Product> findByCategoryId(List<String> categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    public Page<ProductSummaryDTO> findFilteredProduct(FilterDTO filterDTO) {
        List<ProductSummaryDTO> productList;
        Sort sort = Sort.unsorted();
        // If sort filterd
        if (filterDTO.sortBy() != null) {
            if ("des".equals(filterDTO.sortOrder())) {
                sort = Sort.by(Sort.Direction.DESC, filterDTO.sortBy());
            } else {
                sort = Sort.by(Sort.Direction.ASC, filterDTO.sortBy());
            }
        }

        Pageable pageable = PageRequest.of(filterDTO.page(), filterDTO.pageSize(), sort);
        long total;

        boolean isSearching = filterDTO.keyword() != null && !filterDTO.keyword().trim().isEmpty();

        System.out.println("categories" + filterDTO.categories() + ",keyword" + filterDTO.keyword());
        if (isSearching) {
            productList = productRepository.findProductByTitle(filterDTO.keyword(), filterDTO.categories(), pageable);
            System.out.println("convit" + productList);
            total = productRepository.countByKeywordAndCategories(filterDTO.keyword(), filterDTO.categories());
        } else if (filterDTO.categories() == null || filterDTO.categories().isEmpty()) {
            // ⬅️ Case: no category filter → get all
            productList = productRepository.findAllProduct(pageable);
            total = productRepository.countAllProducts();
        } else {
            // ⬅️ Case: filter by categories
            productList = productRepository.findAllAndFilterProduct(filterDTO.categories(), pageable);
            total = productRepository.countFilteredProducts(filterDTO.categories());
        }

        // No sort
        return new PageImpl<>(productList, pageable, total);
    }

    public Page<ProductSummaryDTO> searchProductByTitle(FilterDTO filterDTO) {
        Sort sort = Sort.unsorted();

        if (filterDTO.sortBy() != null && !filterDTO.sortBy().isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.sortOrder())
                    ? Sort.Direction.DESC
                    : Sort.Direction.ASC;
            sort = Sort.by(direction, filterDTO.sortBy());
        }

        Pageable pageable = PageRequest.of(filterDTO.page(), filterDTO.pageSize(), sort);

        List<ProductSummaryDTO> productList =
                productRepository.findProductByTitle(filterDTO.keyword(), filterDTO.categories(), pageable);

        long total = productRepository.countByKeywordAndCategories(filterDTO.keyword(), filterDTO.categories());

        return new PageImpl<>(productList, pageable, total);
    }
}
