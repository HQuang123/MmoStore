package com.swp.mmostore.controller;

import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import com.swp.mmostore.service.OrderService;
import com.swp.mmostore.service.SellerStatisticService;
import com.swp.mmostore.service.ShopService;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class  SellerController {
    @Autowired
    private SellerStatisticService statisticService;

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private ShopService shopService;

    @Autowired private OrderService orderService;

    @GetMapping("/seller/statistic")
    public String viewDashboard(Model model,@RequestParam(defaultValue = "0") int page,
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

}