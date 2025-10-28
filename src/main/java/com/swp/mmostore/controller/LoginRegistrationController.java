package com.swp.mmostore.controller;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.CloudStorageService;
import com.swp.mmostore.service.LoginRegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Controller
public class LoginRegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(LoginRegistrationController.class);

    private static final String ERROR_MSG = "errorMsg";

    private static final String LOGIN_VIEW = "login";

    @Autowired
    CloudStorageService cloudStorageService;

    @Autowired
    LoginRegistrationService loginRegistrationService;
    private UserRepository userRepository;

    private String getSiteURL(HttpServletRequest request) {
        String siteURL = request.getRequestURL().toString();
        return siteURL.replace(request.getServletPath(), "");
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/save-user")
    public String saveUserDetails(@ModelAttribute User user, @RequestParam("file") MultipartFile file, HttpServletRequest request, RedirectAttributes redirectAttributes)
            throws IOException {

        String email = user.getEmail();
        if(loginRegistrationService.getUserByEmail(email) != null){
            redirectAttributes.addFlashAttribute("errorMsg","Email đã tồn tại");
            return "redirect:/register";
        }
        String profileImageUrl;
        if(file != null && !file.isEmpty()){
            try{
                profileImageUrl = cloudStorageService.uploadFile(file);
            } catch (Exception e){
                e.printStackTrace();
                redirectAttributes.addFlashAttribute("errorMsg","Lỗi khi up ảnh");
                return "redirect:/register";
            }
        }
        else{
            profileImageUrl = "https://storage.googleapis.com/mmostore/default.jpg";
        }
        user.setProfileImage(profileImageUrl);
        User savedUser = loginRegistrationService.saveUser(user);
        //Change status back to inactive for verification
        String siteUrl = getSiteURL(request);
        loginRegistrationService.generateRegisterTokenAndSendEmail(savedUser, siteUrl);
        // Generate verification token
        if (!ObjectUtils.isEmpty(savedUser)) {
            redirectAttributes.addFlashAttribute("successMsg", "Bạn đã đăng ký thành công, hãy vào email để nhấn vào đường link xác nhận tài khoản");
        } else {
            redirectAttributes.addFlashAttribute("errorMsg", "Hiện tại dịch vụ đang gián đoạn, hãy thử lại sau");
        }
        return "redirect:/register";
    }

    @GetMapping("/resend-verification")
    public String resendVerificationEmail() { return "resend-verification";}

    @PostMapping("/resend-verification")
    public String processResendVerification(@RequestParam("email") String email, RedirectAttributes redirectAttributes, HttpServletRequest request){
        try{
            String siteUrl = getSiteURL(request);
            loginRegistrationService.resendVerificationEmail(loginRegistrationService.getUserByEmail(email), siteUrl);
            redirectAttributes.addFlashAttribute("successMsg", "Đã gửi link xác nhận !");

        }catch (Exception e){
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi server!");
        }
        return "redirect:/login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, RedirectAttributes redirectAttributes) {
        String result = loginRegistrationService.verifyUserToken(token);
        if(result.equalsIgnoreCase("success")){
            redirectAttributes.addFlashAttribute("successMsg", "Tài khoản đã được xác thực thành công, vui lòng đăng nhập lại !");
        }
        else if("expired".equalsIgnoreCase(result) ){
            redirectAttributes.addFlashAttribute("errorMsg", "Đường dẫn quá hạn, hãy đăng ký lại");
        }
        else{
            redirectAttributes.addFlashAttribute("errorMsg", "Sai mã số !");
        }
        return "redirect:/login";
    }

    // Gửi token qua email
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password"; // form nhập email
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email,
                                        RedirectAttributes redirectAttributes,
                                        Model model) {
        boolean sent = loginRegistrationService.generateResetTokenAndSendEmail(email);
        if (sent) {
            redirectAttributes.addAttribute("email", email); // Spring tự encode
            return "redirect:/confirm-token";
        } else {
            model.addAttribute("error", "Email không tồn tại, hãy kiểm tra lại!");
            return "forgot-password";
        }
    }

    @GetMapping("/confirm-token")
    public String showConfirmTokenPage(@RequestParam("email") String email, Model model) {
        model.addAttribute("email", email);
        return "confirm-token";
    }


    // API verify token AJAX
    @GetMapping("/api/verify-token")
    @ResponseBody
    public Map<String, Boolean> verifyToken(@RequestParam String email, @RequestParam String token) {
        System.out.println("API verify-token called: email=" + email + ", token=" + token);
        boolean valid = loginRegistrationService.verifyResetToken(email, token);
        return Collections.singletonMap("valid", valid);
    }

    // Reset password
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("email", email);
            return "confirm-token";
        }

        boolean success = loginRegistrationService.resetPassword(email, token, password);
        if (success) {
            return "redirect:/login?success=true"; // hoặc LOGIN_VIEW
        } else {
            model.addAttribute("error", "Token không hợp lệ hoặc đã hết hạn!");
            model.addAttribute("email", email);
            return "confirm-token";
        }
    }


}
