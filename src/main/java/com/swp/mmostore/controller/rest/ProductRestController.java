package com.swp.mmostore.controller.rest;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product-list")
public class ProductRestController {
    @Autowired
    private ProductService productService;

    @PostMapping("/filter")
    public Page<ProductSummaryDTO> listProducts(@RequestBody FilterDTO filter) {
        System.out.println("Vit con" + filter);
        return productService.findFilteredProduct(filter);
    }
}
