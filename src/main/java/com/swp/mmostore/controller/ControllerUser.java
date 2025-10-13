package com.swp.mmostore.controller;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.CloudStorageService;
import com.swp.mmostore.service.LoginRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.security.Principal;

@Controller
public class ControllerUser {

    @Autowired
    private LoginRegistrationService userService;

    @Autowired
    private CloudStorageService cloudStorageService;

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
}
