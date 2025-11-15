package com.swp.mmostore.controller;

import com.swp.mmostore.dto.FilterDTO;
import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.repository.CategoryRepository;
import com.swp.mmostore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SearchController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    //    @GetMapping("/search")
//    public String searchProductByTitle(
//            @RequestParam String query,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "8") int size,
//            Model model
//    ) {
//
//        FilterDTO filter = new FilterDTO(null, null, null, page, size, query);
//        Page<ProductSummaryDTO> productPage = productService.findFilteredProduct(filter);
//
//        model.addAttribute("productList", productPage.getContent());
//        model.addAttribute("categoryList", categoryRepository.findAll());
//        model.addAttribute("keyword", query);
//        model.addAttribute("totalPages", productPage.getTotalPages());
//        model.addAttribute("currentPage", page);
//        return "product-list";
//    }
    @GetMapping("/search")
    public String search(
            @RequestParam(name = "query", required = false) String keyword,
            @RequestParam(name = "category", required = false) String categoryId,
            Model model
    ) {
        model.addAttribute("categoryList", categoryRepository.findAll());
        model.addAttribute("searchKeyword", keyword);
        model.addAttribute("selectedCategory", categoryId);
        return "product-list"; // ✅ Reuse the same template
    }

}
