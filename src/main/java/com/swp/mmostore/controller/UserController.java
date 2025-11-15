package com.swp.mmostore.controller;


import com.swp.mmostore.dto.OrderStatisticDTO;
import com.swp.mmostore.dto.TransactionDTO;
import com.swp.mmostore.entity.DepositStatus;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.ShopFee;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ShopFeeRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.*;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class UserController {

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudStorageService cloudStorageService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ShopFeeService shopFeeService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TransactionService transactionService;

    /**
     * Hiển thị trang user_profile.html
     */
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


    /**
     * Trang chỉnh sửa user
     */
    @GetMapping("/user/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);
        return "user_editprofile";
    }

    /**
     * Cập nhật thông tin user
     */

    @PostMapping("/user/update")
    public String updateProfile(
            @Valid @ModelAttribute("user") User updatedUser,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            // Trả về trang edit với lỗi validation
            model.addAttribute("user", updatedUser);
            return "user_editprofile";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User existingUser = userService.getUserByEmail(email);

        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userService.updateUser(existingUser);
            redirectAttributes.addFlashAttribute("successMsg", "Cập nhật thông tin thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Cập nhật thông tin thất bại.");
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

    @GetMapping("/user/transactions")
    public String getUserTransactions(
            Model model,
            @PageableDefault(size = 10, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        String email = MockSecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email);
        // Fetch the paged and sorted data (no filter parameters)
        Page<TransactionDTO> transactionPage = transactionService.getTransactionHistory(user, pageable);

        // Add data to the model for Thymeleaf
        model.addAttribute("transactionPage", transactionPage);
        model.addAttribute("currentPage", pageable.getPageNumber());

        // Add sort info
        model.addAttribute("sortField", pageable.getSort().isSorted() ? pageable.getSort().iterator().next().getProperty() : "createAt");
        model.addAttribute("sortDir", pageable.getSort().isSorted() ? pageable.getSort().iterator().next().getDirection().name() : "DESC");
        return "user/transaction_history"; // Path to your new HTML file
    }

    @GetMapping("/user/seller_register")
    public String getUserSellerRegister(){
        return "seller_register";
    }

    @PostMapping("/user/seller_register")
    public String registerSeller(RedirectAttributes redirectAttributes,
                                 @RequestParam("name") String name,
                                 @RequestParam("description") String description,
                                 @RequestParam("shopImage") MultipartFile shopImage,
                                 HttpSession session) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        if (user == null) {
            session.setAttribute("errorMsg", "Bạn cần đăng nhập để đăng ký bán hàng.");
            return "redirect:/login";
        }

        if (user.getRole().contains("ROLE_SELLER")) {
            redirectAttributes.addFlashAttribute("errorMsg", "Bạn đã có shop, không thể đăng ký thêm.");
            return "redirect:/user/seller_register";
        }

        String shopImageUrl = "https://storage.googleapis.com/mmostore/default-shop.jpg";
        try {
            shopFeeService.chargeRegistrationFee(user);
            // Xử lý ảnh cửa hàng
            if (shopImage != null && !shopImage.isEmpty()) {
                try {
                    shopImageUrl = cloudStorageService.uploadFile(shopImage);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi upload ảnh cửa hàng");
                    return "redirect:/user/seller_register";
                }
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/user/seller_register";
        }


        // Tạo shop
        Shop shop = new Shop(name, description, user, shopImageUrl);

        try {
            shopService.save(shop);
        } catch (jakarta.validation.ConstraintViolationException e) {
            // Gộp tất cả thông báo vi phạm vào một chuỗi
            String errorMessages = e.getConstraintViolations().stream()
                    .map(v -> v.getMessage())
                    .collect(Collectors.joining("<br>"));

            redirectAttributes.addFlashAttribute("errorMsg", errorMessages);
            return "redirect:/user/seller_register";
        }



        if (!user.getRole().contains("ROLE_SELLER")) {
            user.setRole("ROLE_USER,ROLE_SELLER");
            userService.updateUser(user);
        }

        // Cập nhật SecurityContext
        List<GrantedAuthority> authorities = Arrays.stream(user.getRole().split(","))
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r.toUpperCase())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(), authorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

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
            @RequestParam(value = "page", defaultValue = "1") int page, // hiển thị 1-based trong URL
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

        //  page trong Spring Data là 0-based
        int pageSize = 2;
        int currentPageIndex = Math.max(page - 1, 0);

        Pageable pageable = PageRequest.of(currentPageIndex, pageSize);
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

        // Kiểm tra và điều chỉnh khi vượt giới hạn
        if (totalPages == 0) {
            totalPages = 1;
        } else if (page > totalPages) {
            // Nếu người dùng nhập page > tổng số trang → chuyển về last page
            currentPageIndex = totalPages - 1;
            pageable = PageRequest.of(currentPageIndex, pageSize);
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
            page = totalPages; //  hiển thị đúng trên URL
        }

        // --- Gửi dữ liệu ra view ---
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);       // 1-based
        model.addAttribute("totalPages", totalPages);  // 1-based hiển thị

        // render enum Status trong dropdown
        model.addAttribute("statuses", DepositStatus.values());
        model.addAttribute("selectedStatus", status);

        return "order-history";
    }


    @Autowired
    private LoginRegistrationService loginRegistrationService;

    /**
     * Xóa tài khoản user
     */
    @GetMapping("/user/delete")
    public String deleteUser(RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User existingUser = userService.getUserByEmail(email);

        if (existingUser != null) {
            loginRegistrationService.updateUserStatus(false, existingUser.getUserId());
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
