package com.swp.mmostore.controller;

import com.swp.mmostore.dto.OrderForm;
import com.swp.mmostore.entity.ActionType;
import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.Product;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.repository.ProductRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.util.MockSecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class TopUpController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @GetMapping("/user/balance")
    public String productDetails(@RequestParam("action") String action, Model model) {
        Integer userId = MockSecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        Deposit deposit = new Deposit();
        deposit.setUser(user);
        deposit.setActionType(ActionType.Top_up);
        deposit.setPaymentMethod("Momo");
        model.addAttribute("deposit", deposit);
        model.addAttribute("user", user);
        if(action.equalsIgnoreCase("top-up")){
            return "user/top-up";
        }
        return "user/withdraw";
    }
}
