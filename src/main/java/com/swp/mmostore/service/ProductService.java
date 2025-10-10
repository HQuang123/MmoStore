package com.swp.mmostore.service;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

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

    public List<ProductSummaryDTO> findFilteredProduct(FilterDTO filterDTO) {
        // If sort filterd
        if (filterDTO.sortBy() != null) {
            Sort sort;
            if ("des".equals(filterDTO.sortOrder())) {
                sort = Sort.by(Sort.Direction.DESC, filterDTO.sortBy());
            } else {
                sort = Sort.by(Sort.Direction.ASC, filterDTO.sortBy());
            }
            return productRepository.findAllAndFilterProduct(filterDTO.CategoryId(), PageRequest.of(filterDTO.page(), filterDTO.pageSize(), sort));
        }

        // No sort
        return productRepository.findAllProduct(PageRequest.of(filterDTO.page(), filterDTO.pageSize()));
    }
}
