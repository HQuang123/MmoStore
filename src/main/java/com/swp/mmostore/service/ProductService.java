package com.swp.mmostore.service;

import com.swp.mmostore.dto.*;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.Rating;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.repository.RatingRepository;
import com.swp.mmostore.repository.ShopRepository;
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

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private RatingRepository ratingRepository;

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
        if (filterDTO.categories() == null || filterDTO.categories().isEmpty()) {
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

    public Product findById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }

    public List<ProductSummaryDTO> findRelated(Integer id) {
        // Ví dụ: lấy 4 sản phẩm đầu tiên khác sản phẩm hiện tại
        return productRepository.findAllProduct(Pageable.unpaged())
                .stream()
                .filter(p -> !p.id().equals(id))
                .limit(4)
                .toList();
    }

    public ProductDetailDTO findProductDetailById(Integer id) {
        return productRepository.findProductById(id);
    }

    public ShopSummaryDTO findShopSummaryById(Integer id) {
        return shopRepository.findShopId(id);
    }

    public List<RatingDTO> getRatingsByProduct(Integer productId) {
        return ratingRepository.findAllByProductId(productId);
    }
}
