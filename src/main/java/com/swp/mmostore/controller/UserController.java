package com.swp.mmostore.controller;


import com.swp.mmostore.dto.OrderStatisticDTO;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class UserController {

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private PasswordEncoder passwordEncoder;


    /** Hiển thị trang user_profile.html */
    @GetMapping("/user/detail")
    public String viewUserDetail(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        // Lấy tên blob (chỉ phần cuối)
//        String blobName = null;
//        if (user.getProfileImage() != null && user.getProfileImage().contains("/")) {
//            blobName = user.getProfileImage().substring(user.getProfileImage().lastIndexOf("/") + 1);
//        }

        model.addAttribute("user", user);
        //model.addAttribute("blobName", blobName);

        return "user_profile"; // -> hiển thị HTML
    }

    @GetMapping("/user/image")
    public ResponseEntity<byte[]> getUserImage(@RequestParam("blobName") String blobName) {
        try {
            byte[] content = cloudStorageService.downloadFile(blobName);

            // Có thể nhận contentType động từ blob nếu cần (nếu bạn lưu nó trong DB)
            MediaType contentType = MediaType.IMAGE_JPEG;
            if (blobName.endsWith(".png")) contentType = MediaType.IMAGE_PNG;
            else if (blobName.endsWith(".webp")) contentType = MediaType.valueOf("image/webp");

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .contentLength(content.length)
                    .body(content);

        } catch (IOException e) {
            System.err.println("Lỗi tải ảnh từ GCS: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/user/editimage")
    public String handleEditImage(@RequestParam("imageFile") MultipartFile file,
                                  RedirectAttributes redirectAttributes,
                                  Principal principal) {
        try {
            String email = principal.getName();
            User user = userService.getUserByEmail(email);

            // Upload ảnh lên GCS
            String imageUrl = cloudStorageService.uploadFile(file);
            user.setProfileImage(imageUrl);
            userService.updateUser(user);

            redirectAttributes.addFlashAttribute("successMsg", "Image update success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("successMsg", "Image update fail");
        }

        return "redirect:/user/detail";
    }


    /**  Trang chỉnh sửa user */
    @GetMapping("/user/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);
        return "user_editprofile";
    }

    /** Cập nhật thông tin user */
    @PostMapping("/user/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User existingUser = userService.getUserByEmail(email);

        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("successMsg", "Information update success");
        }else{
            redirectAttributes.addFlashAttribute("successMsg", "Information update fail");
        }
        return "redirect:/user/detail";
    }

    @PostMapping("/user/reset-password")
    @Transactional
    public String resetPassword(@RequestParam String oldPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {

        String email = principal.getName();
        User user = userService.getUserByEmail(email);

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu hiện tại không đúng!");
            return "redirect:/user/detail";
        }

        // Kiểm tra xác nhận
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMsg", "Mật khẩu xác nhận không khớp!");
            return "redirect:/user/detail";
        }

        userService.updatePassword(email, newPassword, passwordEncoder);
        redirectAttributes.addFlashAttribute("successMsg", "Đổi mật khẩu thành công!");
        return "redirect:/user/detail";
    }





    @GetMapping("/user/seller_register")
    public String showSellerRegisterPage() {
        return "seller_register";
    }


    @PostMapping("/user/seller_register")
    public String registerSeller(RedirectAttributes redirectAttributes,
                                 @RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("shopImage") MultipartFile shopImage,
                                 HttpSession session) {

        // Lấy thông tin user đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        User user = userService.getUserByEmail(email);
        if (user == null) {
            session.setAttribute("errorMsg", "Bạn cần đăng nhập để đăng ký bán hàng.");
            return "redirect:/login";
        }

        try {
            walletService.deductMoney(user.getUserId(), BigDecimal.valueOf(200000), "MMO");
        } catch (RuntimeException e) {
            session.setAttribute("errorMsg", e.getMessage());
            return "redirect:/user/seller_register";
        }

        // Xử lý upload ảnh cửa hàng
        String shopImageUrl;
        if (shopImage != null && !shopImage.isEmpty()) {
            try {
                shopImageUrl = cloudStorageService.uploadFile(shopImage);
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("errorMsg", "Lỗi khi upload ảnh cửa hàng");
                return "redirect:/user/detail";
            }
        } else {
            shopImageUrl = "https://storage.googleapis.com/mmostore/default-shop.jpg"; // default image
        }


        // Tạo và lưu shop mới
        Shop shop = new Shop(name,description,user,shopImageUrl);
        shopService.save(shop);

        // Cập nhật role người dùng thành SELLER nếu chưa có
        if (!user.getRole().contains("ROLE_SELLER")) {
            user.setRole("ROLE_USER,ROLE_SELLER");
            userService.updateUser(user);
        }

        redirectAttributes.addFlashAttribute("successMsg", "Đăng ký cửa hàng thành công! Hãy bắt đầu bán hàng ngay.");
        return "redirect:/seller/statistic";
    }


    @GetMapping("/user/orders")
    public String orderHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "productName", required = false) String productName,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "paymentMethod", required = false) String paymentMethod,
            @RequestParam(value = "minTotal", required = false) BigDecimal minTotal,
            @RequestParam(value = "maxTotal", required = false) BigDecimal maxTotal,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model
    ) {
        User user = userService.getUserByEmail(userDetails.getUsername());
        Integer userId = user.getUserId();

        // --- Chuẩn hóa input ---
        orderId = (orderId != null && !orderId.isBlank()) ? orderId : null;
        status = (status != null && !status.isBlank()) ? status : null;
        paymentMethod = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : null;
        productName = (productName != null && !productName.isBlank()) ? productName : null;

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        // --- Validate page ---
        if (page < 0) page = 0;
        int pageSize = 2;

        Pageable pageable = PageRequest.of(page, pageSize);
        Page<OrderStatisticDTO> orderPage = orderService.getOrderHistory(
                userId,
                orderId,
                productName,
                startDateTime,
                endDateTime,
                status,
                paymentMethod,
                minTotal,
                maxTotal,
                pageable
        );

        int totalPages = orderPage.getTotalPages();

        // --- Nếu page vượt quá tổng số trang, set về last page và gọi lại service ---
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
            pageable = PageRequest.of(page, pageSize);
            orderPage = orderService.getOrderHistory(
                    userId,
                    orderId,
                    productName,
                    startDateTime,
                    endDateTime,
                    status,
                    paymentMethod,
                    minTotal,
                    maxTotal,
                    pageable
            );
        }

        // --- Add model attributes ---
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        // render enum Status trong dropdown
        model.addAttribute("statuses", DepositStatus.values());
        model.addAttribute("selectedStatus", status);

        return "order-history";
    }



    @Autowired
    private  LoginRegistrationService  loginRegistrationService;
    /** Xóa tài khoản user */
    @GetMapping("/user/delete")
    public String deleteUser(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User existingUser = userService.getUserByEmail(email);

        if (existingUser != null) {
            loginRegistrationService.updateUserStatus(false,existingUser.getUserId());
            //userService.deleteUser(existingUser.getUserId());
            redirectAttributes.addFlashAttribute("successMsg", "Your account has been deleted successfully.");
            // Sau khi xóa, đăng xuất người dùng
            SecurityContextHolder.clearContext();
            return "redirect:/logout"; // hoặc redirect về trang chủ tùy logic app
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "User not found!");
            return "redirect:/user/detail";
        }
    }


}
