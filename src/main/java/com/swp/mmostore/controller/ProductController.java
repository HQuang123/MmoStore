package com.swp.mmostore.controller;

import com.swp.mmostore.dto.*;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.Rating;
import com.swp.mmostore.repository.CategoryRepository;
import com.swp.mmostore.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping("/productList")
    public String productList(Model model) {
        model.addAttribute("productList", productService.findFilteredProduct(new FilterDTO()));
        System.out.println("Cai deo:" + productService.findFilteredProduct(new FilterDTO()));
        model.addAttribute("categoryList", categoryRepository.findAll());
        return "product-list";
    }

//    @PostMapping("/productList")
//    public String productListPost(@RequestParam(name = "filters", required = false) List<String> filters, Model model) {
//        if (filters != null && !filters.isEmpty()) {
//            model.addAttribute("productList", productService.findByCategoryId(filters));
//        } else {
//            model.addAttribute("productList", productService.loadAllProduct());
//        }
//        model.addAttribute("categoryList", categoryRepository.findAll());
//        model.addAttribute("checkedCategory", filters);
//        return "product-list";
//    }

    @PostMapping("/productList")
    public String productListPost(@RequestParam(name = "filters", required = false) FilterDTO filterDTO,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "8") int size,
                                  Model model) {
        if (filterDTO != null) {
            model.addAttribute("productList", productService.findFilteredProduct(filterDTO));
        } else {
            model.addAttribute("productList", productService.loadAllProduct());
        }
        model.addAttribute("categoryList", categoryRepository.findAll());
        model.addAttribute("checkedCategory", filterDTO);
        return "product-list";
    }

    @GetMapping("/product/{id}")
    public String showProductDetail(@PathVariable Integer id, Model model) {
        ProductDetailDTO product = productService.findProductDetailById(id);
        ShopSummaryDTO shop = productService.findShopSummaryById(product.shopId());
        List<ProductSummaryDTO> related = productService.findRelated(id);
        List<RatingDTO> ratings = productService.getRatingsByProduct(id);
        model.addAttribute("product", product);
        model.addAttribute("shop", shop);
        model.addAttribute("relatedProducts", related);
        model.addAttribute("ratings", ratings);

        return "product-detail"; // Trỏ đến file product-detail.html
    }
}
