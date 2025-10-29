package com.swp.mmostore.controller;

import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.entity.DepositStatus;
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

        Page<ProductSalesDTO> reportPage = shopService.getSoldProductsByShop(shopId, page, 1);

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
            @RequestParam(defaultValue = "0") String pageParam,
            @RequestParam(value = "minQuantity", required = false) Integer minQuantity,
            @RequestParam(value = "minTotal", required = false) BigDecimal minTotal,
            @RequestParam(value = "maxTotal", required = false) BigDecimal maxTotal,
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) String status,
            Model model
    ) {
        int page = 0;
        try {
            page = Integer.parseInt(pageParam);
            if (page < 0) page = 0;
        } catch (NumberFormatException e) {
            page = 0;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        Shop shop = shopService.findByUserId(user.getUserId());

        // --- Chuẩn hóa input ---
        status = (status != null && !status.isBlank()) ? status : null;
        minQuantity = (minQuantity != null && minQuantity > 0) ? minQuantity : null;
        minTotal = (minTotal != null && minTotal.compareTo(BigDecimal.ZERO) > 0) ? minTotal : null;
        maxTotal = (maxTotal != null && maxTotal.compareTo(BigDecimal.ZERO) > 0) ? maxTotal : null;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        Pageable pageable = PageRequest.of(page, 1, Sort.by("createAt").descending());

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

        // --- Add model attributes ---
        model.addAttribute("shop", shop);
        model.addAttribute("orderList", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        model.addAttribute("statuses", com.swp.mmostore.entity.DepositStatus.values());
        model.addAttribute("selectedStatus", status);

        return "seller/orders";
    }





}