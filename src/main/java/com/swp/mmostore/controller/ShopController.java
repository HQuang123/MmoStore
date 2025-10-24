package com.swp.mmostore.controller;

import com.swp.mmostore.dto.ProductSummaryDTO;
import com.swp.mmostore.dto.ShopViewDTO;
import com.swp.mmostore.service.ProductService;
import com.swp.mmostore.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;
    private final ProductService productService; // --- INJECT PRODUCT SERVICE ---

    /**
     * Handles the main shop profile page
     */
    @GetMapping("/{shopId}")
    public String viewShopPage(@PathVariable Integer shopId, Model model) {
            ShopViewDTO shopDTO = shopService.findShopViewById(shopId);
            model.addAttribute("shop", shopDTO);
            return "shop-information";
    }

    /**
     * Handles the "View All Products" button for a specific shop
     */
    @GetMapping("/{shopId}/products")
    public String viewShopProducts(
            @PathVariable Integer shopId,
            Model model,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "9") int size,
            @RequestParam(value = "sort", defaultValue = "createAt,desc") String sort) {

            ShopViewDTO shop = shopService.findShopViewById(shopId);
            model.addAttribute("shop", shop);

            String[] sortParams = sort.split(",");
            Sort.Direction direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sortOrder = Sort.by(direction, sortParams[0]);
            Pageable pageable = PageRequest.of(page, size, sortOrder);

            Page<ProductSummaryDTO> productPage = productService.findProductsByShopId(shopId, pageable);
            model.addAttribute("productPage", productPage);

            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());

            return "shop-products";

    }
}
