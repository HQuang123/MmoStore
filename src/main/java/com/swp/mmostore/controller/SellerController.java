package com.swp.mmostore.controller;

import com.swp.mmostore.dto.ProductSalesDTO;
import com.swp.mmostore.dto.ShopOrderHistoryDTO;
import com.swp.mmostore.dto.ShopStatisticDTO;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.*;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
public class  SellerController {
    @Autowired
    private SellerStatisticService statisticService;

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CloudStorageService cloudStorageService;

    @GetMapping("/seller/statistic")
    public String viewDashboard(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        Shop shop = shopService.findByUserId(user.getUserId());
        ShopStatisticDTO dashboard = statisticService.getStatisticdData(shop.getShopId());

        Integer shopId = shop.getShopId();
        int pageSize = 1;

        // --- Validate page >= 0 ---
        if (page < 0) page = 0;

        // --- Tạo Pageable tạm để lấy tổng số trang ---
        Pageable tempPageable = PageRequest.of(0, pageSize);
        Page<ProductSalesDTO> tempPage = shopService.getSoldProductsByShop(shopId, 0, pageSize);
        int totalPages = tempPage.getTotalPages();

        // --- Nếu page vượt quá tổng số trang, set về last page ---
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
        }

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<ProductSalesDTO> reportPage = shopService.getSoldProductsByShop(shopId, page, pageSize);

        // --- Add model attributes ---
        model.addAttribute("shop", shop);
        model.addAttribute("reportList", reportPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reportPage.getTotalPages());
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("seller", user);

        return "seller/statistic";
    }



    @GetMapping("/seller/orders")
    public String viewShopOrders(
            @RequestParam(defaultValue = "0") int page,
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

        // --- Tạo Pageable với page >= 0 ---
        if (page < 0) page = 0;
        int pageSize = 2;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());

        // --- Gọi service 1 lần để lấy Page ---
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

        int totalPages = orderPage.getTotalPages();

        // --- Nếu page vượt quá totalPages, reset về last page ---
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
            pageable = PageRequest.of(page, pageSize, Sort.by("createAt").descending());
            orderPage = orderService.getFilteredOrders(
                    shop.getShopId(),
                    minQuantity,
                    minTotal,
                    maxTotal,
                    startDateTime,
                    endDateTime,
                    status,
                    pageable
            );
        }

        // --- Add model attributes ---
        model.addAttribute("shop", shop);
        model.addAttribute("orderList", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        model.addAttribute("minQuantity", minQuantity);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("status", status);

        model.addAttribute("statuses", DepositStatus.values());
        model.addAttribute("selectedStatus", status);

        return "seller/orders";
    }


    //CHỈNH SỬA THÔNG TIN SHOP
    @GetMapping("/seller/shop/edit")
    public String editShopForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        Shop shop = shopService.findByUserId(user.getUserId());
        model.addAttribute("shop", shop);
        model.addAttribute("seller", user);
        return "seller/edit-shop";
    }

    @PostMapping("/seller/shop/update")
    public String updateShopInfo(@ModelAttribute Shop updatedShop,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal) {
        try {
            // Lấy thông tin người dùng hiện tại
            String email = principal.getName();
            User user = userService.getUserByEmail(email);

            // Lấy shop hiện tại của người dùng
            Shop currentShop = shopService.findByUserId(user.getUserId());
            if (currentShop == null) {
                redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy shop của bạn!");
                return "redirect:/seller/statistic";
            }

            // Cập nhật các trường text
            currentShop.setName(updatedShop.getName());
            currentShop.setDescription(updatedShop.getDescription());

            // Nếu có upload ảnh mới thì upload lên GCS
            if (file != null && !file.isEmpty()) {
                String imageUrl = cloudStorageService.uploadFile(file); // Dịch vụ upload GCS
                currentShop.setShopImageUrl(imageUrl);
            }

            // Lưu lại thông tin vào DB
            shopService.save(currentShop);

            // Thông báo thành công
            redirectAttributes.addFlashAttribute("successMsg", "Update success");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMsg", "Update fail");
        }

        return "redirect:/seller/statistic";
    }




}