package com.swp.mmostore.controller;

import com.swp.mmostore.service.HomepageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomepageController {

    @Autowired
    private HomepageService homepageService;

    @GetMapping("/")
    public String homepage(Model model) {
        model.addAttribute("latestSixActiveCategory", homepageService.getTopSixCategories());
        model.addAttribute("twelveLastestProducts", homepageService.getTopTwelveProducts());
        return "index";
    }
}
