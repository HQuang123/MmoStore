package com.swp.mmostore.controller;


import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.Shop;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.CloudStorageService;
import com.swp.mmostore.service.LoginRegistrationService;
import com.swp.mmostore.service.OrderService;
import com.swp.mmostore.service.ShopService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;

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

    /** Hiển thị trang user_profile.html */
    @GetMapping("/user/detail")
    public String viewUserDetail(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);

        // Lấy tên blob (chỉ phần cuối)
        String blobName = null;
        if (user.getProfileImage() != null && user.getProfileImage().contains("/")) {
            blobName = user.getProfileImage().substring(user.getProfileImage().lastIndexOf("/") + 1);
        }

        model.addAttribute("user", user);
        model.addAttribute("blobName", blobName);

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

            redirectAttributes.addFlashAttribute("success", "Ảnh đã được cập nhật!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi upload ảnh!");
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
    public String updateProfile(@ModelAttribute("user") User updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User existingUser = userService.getUserByEmail(email);

        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userService.updateUser(existingUser);
        }
        return "redirect:/user/detail";
    }

    @GetMapping("/user/seller_register")
    public String showSellerRegisterPage() {
        return "seller_register";
    }


    @PostMapping("/user/seller_register")
    public String registerSeller(@RequestParam("name") String name,
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
            user.setRole(user.getRole()+",ROLE_SELLER");
            userService.updateUser(user);
        }

        session.setAttribute("successMsg", "Đăng ký cửa hàng thành công! Hãy bắt đầu bán hàng ngay.");
        return "redirect:/user/detail";
    }

    @GetMapping("/user/orders")
    public String orderHistory(
            Model model,
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page) {

        // 🧍‍♂️ Lấy user đang đăng nhập
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return "redirect:/login";
        }

        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        if (user == null) {
            return "redirect:/login";
        }

        int pageSize = 3;
        Page<Order> orderPage;

        // Tìm theo Mã đơn hàng
        if (orderId != null && !orderId.isEmpty()) {
            orderPage = orderService.findByUserAndOrderId(user.getUserId(), orderId, page, pageSize);

            //  Lọc theo khoảng thời gian
        } else if (startDate != null && endDate != null) {
            orderPage = orderService.findByUserAndDateRange(user.getUserId(), startDate, endDate, page, pageSize);

            //  Nếu không có filter nào
        } else {
            orderPage = orderService.getOrdersByUser(user.getUserId(), page, pageSize);
        }

        // 🧩 Gửi dữ liệu sang View
        model.addAttribute("orderPage", orderPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());

        // 🧠 Giữ lại các tham số tìm kiếm để hiển thị lại trong form
        model.addAttribute("paramOrderId", orderId);
        model.addAttribute("paramStartDate", startDate);
        model.addAttribute("paramEndDate", endDate);

        return "order-history";
    }


}
