package com.swp.mmostore.rest;

import com.swp.mmostore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/product-list")
public class ProductRestController {
    @Autowired
    private ProductService productService;
}
