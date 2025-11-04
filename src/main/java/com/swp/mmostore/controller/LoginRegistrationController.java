package com.swp.mmostore.controller;

import com.swp.mmostore.entity.PasswordResetToken;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.PasswordResetTokenRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.CloudStorageService;
import com.swp.mmostore.service.LoginRegistrationService;
import com.swp.mmostore.service.RecaptchaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.bouncycastle.math.raw.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.xml.validation.Validator;
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

    @Autowired
    RecaptchaService recaptchaService;
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Autowired
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
    public String register(Model model){
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/save-user")
    public String saveUserDetails(@Valid @ModelAttribute User user,BindingResult bindingResult, @RequestParam("file") MultipartFile file, HttpServletRequest request, RedirectAttributes redirectAttributes, @RequestParam("g-recaptcha-response") String recaptchaResponse) throws IOException {
        if(bindingResult.hasErrors()){
            return "register";
        }
        boolean isRecaptchaValid = recaptchaService.validateRecaptcha(recaptchaResponse);
        if(!isRecaptchaValid){
            redirectAttributes.addFlashAttribute("errorMessage", "CAPTCHA validation failed. Please try again.");
            return "redirect:/register";
        }
        String email = user.getEmail();
        User existingUser = userRepository.findByEmail(email);
        if(loginRegistrationService.getUserByEmail(email) != null && existingUser.getStatus() && existingUser.getAccountStatusNonLocked()){
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
        user.setStatus(false);
        //register user and change status back to inactive for verification
        String siteUrl = getSiteURL(request);
        loginRegistrationService.generateRegisterTokenAndSendEmail(user, siteUrl);

        if (!ObjectUtils.isEmpty(user)) {
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
            redirectAttributes.addFlashAttribute("successMsg", "Nếu email tồn tại, link xác nhận mới đã được gửi.");
        }catch (Exception e){
            redirectAttributes.addFlashAttribute("successMsg", "Nếu email tồn tại, link xác nhận mới đã được gửi.");
        }
        return "redirect:/login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam("token") String token, @RequestParam("userEmail") String email, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);

        if(user == null){
            //avoid to let the hacker know if the mail non exists
            redirectAttributes.addFlashAttribute("errorMsg", "Đường dẫn quá hạn, hãy đăng ký lại");
        }else{
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
                                        Model model, HttpServletRequest request) {

        User user = userRepository.findByEmail(email);

        if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
            // Không tồn tại hoặc đã bị xóa
            redirectAttributes.addFlashAttribute("successMsg", "Email không tồn tại hoặc đã bị xóa!");
            return "redirect:/forgot-password";
        }

        // Nếu tồn tại và đang hoạt động
        String siteUrl = getSiteURL(request);
        loginRegistrationService.generateResetTokenAndSendEmail(email, siteUrl);

        redirectAttributes.addFlashAttribute("successMsg", "Đường dẫn đặt lại mật khẩu đã được gửi đến email của bạn!");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam("token") String token, @RequestParam("userEmail") String email, Model model, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(email);
        if(user == null){
            redirectAttributes.addFlashAttribute("errorMsg", "Link đã hết hạn");
            return "redirect:/login";
        }
        else{
            String validationResult = loginRegistrationService.verifyResetToken(token);
            if(!"valid".equalsIgnoreCase(validationResult)){
                redirectAttributes.addFlashAttribute("errorMsg", "Link đã hết hạn");
                return "redirect:/login";
            }
        }
        //add token to the model so -> can pass to the post handler
        model.addAttribute("token", token);
        return "reset-password";
    }


    // Reset password
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
                                       @RequestParam("password") String password,
                                       RedirectAttributes redirectAttributes) {
        String validateResult = loginRegistrationService.verifyResetToken(token);
        if(!validateResult.equalsIgnoreCase("valid")){
            redirectAttributes.addFlashAttribute("errorMsg","Link bị lỗi hoặc hết hạn");
            return "redirect:/login";
        }
        loginRegistrationService.resetPassword(token, password);
        redirectAttributes.addFlashAttribute("successMsg", "Password reset success");
        return "redirect:/login";
    }


}
