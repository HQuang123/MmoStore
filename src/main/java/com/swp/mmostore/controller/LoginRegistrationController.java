package com.swp.mmostore.controller;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.service.LoginRegistrationService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Controller
public class LoginRegistrationController {
    private static final Logger logger = LoggerFactory.getLogger(LoginRegistrationController.class);

    private static final String ERROR_MSG = "errorMsg";

    private static final String LOGIN_VIEW = "login";

    @Autowired
    LoginRegistrationService userService;

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/register")
    public String register(){
        return "register";
    }

    @PostMapping("/save-user")
    public String saveUserDetails(@ModelAttribute User user, @RequestParam("file") MultipartFile file, Model model, HttpSession session)
    throws IOException {

        String email = user.getEmail();
        if(userService.getUserByEmail(email) == null){
            //user not input image -> default image, if input, the name of file is the name of image file
            //user image will always be saved into the classes
            String profileImage = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
            user.setProfileImage(profileImage);
            User saveUser = userService.saveUser(user);

            if(!ObjectUtils.isEmpty(saveUser)){
                if(!file.isEmpty()){
                    File saveFile = new ClassPathResource("/static/images").getFile();
                    System.out.println("Save file is " + saveFile);

                    //full-path
                    //remember that intelliij does not copy an empty directory from src/resources to target/classes in exploded build mode- which is a mode instead of using fat jar, it launch main class from target folder
                    Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+"profile_img"+File.separator+file.getOriginalFilename());
                    System.out.println("Path for Profile Image :"+path);

                    //now: if same file with name exist -> replace existing
                    Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                }
                session.setAttribute("successMsg","Bạn đã đăng ký thành công");
            }
            else{
                session.setAttribute("errorMsg","500 error");
            }
            //avoid resubmission -> change the url
        }
        else{
            session.setAttribute("errorMsg","Email is already exist");
        }
        return "redirect:/register";
    }

    // ----------------- Bước 1: Nhập email để gửi token -----------------
    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password"; // form nhập email
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, Model model) {
        boolean sent = userService.generateResetTokenAndSendEmail(email);
        if (sent) {
            model.addAttribute("email", email);
            return "confirm-token"; // form nhập token
        } else {
            model.addAttribute("error", "Email không tồn tại hoặc gửi email thất bại!");
            return "forgot-password";
        }
    }

    // ----------------- Bước 2: Xác thực token -----------------
    @PostMapping("/confirm-token")
    public String confirmToken(@RequestParam("email") String email,
                               @RequestParam("token") String token,
                               Model model) {
        if (userService.verifyResetToken(email, token)) {
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "reset-password"; // form nhập password mới
        } else {
            model.addAttribute("error", "Token không hợp lệ!");
            model.addAttribute("email", email);
            return "confirm-token";
        }
    }

    // ----------------- Bước 3: Reset mật khẩu -----------------
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam("email") String email,
                                @RequestParam("token") String token,
                                @RequestParam("password") String password,
                                @RequestParam("confirmPassword") String confirmPassword,
                                Model model) {

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "reset-password";
        }

        boolean success = userService.resetPassword(email, token, password);
        if (success) {
            return LOGIN_VIEW;
        } else {
            model.addAttribute("error", "Token không hợp lệ!");
            model.addAttribute("email", email);
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}
