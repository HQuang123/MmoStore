package com.swp.mmostore.controller;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ControllerUser {
    @Autowired
    LoginRegistrationService userService;

    @GetMapping("/user/detail")
    public String viewUserDetail(HttpSession session, Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // username/email đã đăng nhập


        User user = userService.getUserByEmail(email);


        model.addAttribute("user", user);

        return "user_profile";
    }
    // Trang chỉnh sửa
    @GetMapping("/user/edit")
    public String editProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userService.getUserByEmail(email);
        model.addAttribute("user", user);
        return "user_editprofile";
    }

    // Cập nhật thông tin người dùng
    @PostMapping("/user/update")
    public String updateProfile(@ModelAttribute("user") User updatedUser) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        // Lấy user hiện tại trong DB
        User existingUser = userService.getUserByEmail(email);
        if (existingUser != null) {
            existingUser.setName(updatedUser.getName());
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
            userService.updateUser(existingUser);
        }

        return "redirect:/user/detail";
    }
    }

