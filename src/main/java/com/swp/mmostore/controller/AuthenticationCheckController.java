package com.swp.mmostore.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthenticationCheckController {
    @GetMapping("/profile")
    public String profile(Model model){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principle = auth.getPrincipal();
        String username = auth.getName();
        if(principle.equals("anonymousUser")){
            return "unauthorized";
        }
        model.addAttribute("username", username);
        return "authorized";
    }
}
