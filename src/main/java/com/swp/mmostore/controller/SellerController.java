package com.swp.mmostore.controller;

import com.swp.mmostore.dto.*;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.CategoryRepository;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SellerController {
    @Autowired
    private SellerStatisticService statisticService;

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/seller/statistic")
    public String viewDashboard(Model model, @RequestParam(defaultValue = "0") int page,
                                HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        Shop shop = shopService.findByUserId(user.getUserId());
        ShopStatisticDTO dashboard = statisticService.getStatisticdData(shop.getShopId());

        Integer shopId = shop.getShopId();

        Page<ProductSalesDTO> reportPage = shopService.getSoldProductsByShop(shopId, page, 10);

        model.addAttribute("shop", shop);
        model.addAttribute("reportList", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());


        model.addAttribute("shop", shop);
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("seller", user);

        return "seller/statistic";
    }


    @GetMapping("/seller/orders")
    public String viewShopOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer minQuantity,
            @RequestParam(required = false) BigDecimal minTotal,
            @RequestParam(required = false) BigDecimal maxTotal,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        // Convert LocalDate → LocalDateTime cho JPQL filter
        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(23, 59, 59) : null;

        // Pageable cho phân trang
        Pageable pageable = PageRequest.of(page, 10, Sort.by("createAt").descending());

        // Gọi service đúng method và tham số
        Page<ShopOrderHistoryDTO> orderPage = orderService.getFilteredOrders(
                shop.getShopId(),
                minQuantity,
                minTotal,
                maxTotal,
                startDateTime,
                endDateTime,
                status,
                pageable
        );

        model.addAttribute("shop", shop);
        model.addAttribute("orderList", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        // Giữ lại giá trị lọc
        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        return "seller/orders";
    }

    @GetMapping("/seller/products")
    public String viewShopProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createAt").descending());

        // Gọi service để lấy danh sách sản phẩm với điều kiện lọc
        Page<ProductSummaryDTO> productPage = productService.getFilteredProductsByShop(
                shop.getShopId(),
                keyword,
                category,
                minPrice,
                maxPrice,
                pageable
        );

        // Thêm dữ liệu cho view
        model.addAttribute("shop", shop);
        model.addAttribute("productList", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());

        // Giữ lại giá trị filter
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "seller/products";

    }

    @GetMapping("/seller/products/add")
    public String showAddProductForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());
        model.addAttribute("shop", shop);

        model.addAttribute("productForm", new ProductFormDTO());
        model.addAttribute("categories", categoryRepository.findAll());
        return "seller/create-product-form";
    }

    @GetMapping("/seller/products/edit/{id}")
    public String showEditForm(@PathVariable("id") Integer id, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());


        Product product = productRepository.findById(id).orElse(null);
        if (product == null || !product.getShop().getShopId().equals(shop.getShopId())) {
            return "redirect:/seller/products"; // Prevent editing others' products
        }

        ProductFormDTO form = new ProductFormDTO();
        form.setId(product.getProductId());
        form.setTitle(product.getTitle());
        form.setDescription(product.getDescription());
        form.setPrice(product.getPrice());
        form.setExistingImageUrl(product.getProductImageUrl());
        form.setCategoryId(product.getCategory().getCategoryId());
//        form.setFields(productService.getConvertFields(product.getFields()));
        form.setFields(product.getFields());

        model.addAttribute("shop", shop);
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("productForm", form);

        return "seller/create-product-form";
    }

    @PostMapping("/seller/products/save")
    public String saveProduct( @ModelAttribute("productForm") ProductFormDTO form,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              HttpSession session) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không hợp lệ!");
            return "seller/create-product-form";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByEmail(auth.getName());
        Shop shop = shopService.findByUserId(user.getUserId());

        productService.saveProduct(form, shop);

        redirectAttributes.addFlashAttribute("success", "Lưu sản phẩm thành công!");
        return "redirect:/seller/products";
    }

    @GetMapping("/seller/items/create")
    public String showCreateItemForm(Model model, Principal principal) {

        User user = userService.getUserByEmail(principal.getName());
        Shop shop = shopService.findByUserId(user.getUserId());
        List<Product> products = productService.getProductsBySeller(shop);
        model.addAttribute("products", products);
        return "seller/create-item-form";
    }

    @PostMapping("/seller/items/save")
    public String saveItem(@RequestParam("productId") Integer productId,
                           @RequestParam Map<String, String> params,
                           RedirectAttributes redirectAttributes) {
        Product product = productService.findById(productId);

        // Extract only dynamic fields
        Map<String, Object> valueMap = new HashMap<>();
        for (String key : product.getFields().keySet()) {
            if (params.containsKey(key)) {
                valueMap.put(key, params.get(key));
            }
        }

//        itemService.createItem(product, valueMap);
        redirectAttributes.addFlashAttribute("success", "Item created successfully!");
        return "redirect:/seller/items/create";
    }

    @GetMapping("/seller/products/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        productRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Product deleted successfully!");
        return "redirect:/seller/products";
    }

}