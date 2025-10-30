package com.swp.mmostore.controller.rest;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.entity.Item;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}/fields")
    public Map<String, Object> getProductFields(@PathVariable("id") Integer productId) {
        Product product = productService.findById(productId);
        return product.getFields();
    }

    @PostMapping("/seller/items/save")
    public ResponseEntity<?> saveItem(@RequestBody Map<String, Object> payload) {
        try {
            Object rawProductId = payload.get("productId");
            Integer productId = (rawProductId != null)
                    ? Integer.parseInt(rawProductId.toString())
                    : null;

            Map<String, Object> fields = (Map<String, Object>) payload.get("fields");

            Product product = productService.findById(productId);
            if (product == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Product not found."));
            }

            Item item = new Item();
            item.setProduct(product);
            item.setIsSold(false);
            item.setValue(fields);

            productService.saveItem(item);

            return ResponseEntity.ok(Map.of("message", "Item created successfully!"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }
}
