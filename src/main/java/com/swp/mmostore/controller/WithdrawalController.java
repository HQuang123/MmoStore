package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Bank;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawRequestRepository;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WithdrawalController {
    private final UserRepository userRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;

    @ModelAttribute //this method will automatically model.addAttribute for every method in this class
    public void addCommonAttributes(Model model) {
        String email = MockSecurityUtils.getCurrentUserEmail();
        log.info("Current user id: {}", email);
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/wallet/withdraw")
    public String convit(Model model) {
        model.addAttribute("banks", Bank.listAll());
        model.addAttribute("withdrawalRequest", new Withdrawal());
        return "user/withdraw";
    }
    //
    @PostMapping("/user/wallet/withdraw")
    public String submitWithdrawal(@Valid @ModelAttribute Withdrawal withdrawal, RedirectAttributes redirectAttributes, BindingResult bindingResult, Model model) {
        User user = (User) model.getAttribute("user");
        if (bindingResult.hasErrors()) {
            model.addAttribute("banks", Bank.listAll());
            log.info("Binding result has errors");
            return "user/withdraw";
            //Todo: resolve this error
        }
        return "redirect:/user/balance?action=top-up";

//        // Check 1: Existing Pending Request
//        if (withdrawRequestRepository.findByUserAndStatus(user, "PENDING").isPresent()) {
//            redirectAttributes.addFlashAttribute("error", "Bạn dã có yêu cầu rút tiền đang được xử lý!");
//            return "user/withdraw";
//        }
//
//        //2. Check balance
//        if (user.getBalance().compareTo(withdrawal.getAmount()) < 0) {
//            redirectAttributes.addFlashAttribute("error", "Số dư không khả dụng!");
//            return "user/withdraw";
//        }
//
//        //set pending
//        withdrawal.setStatus("PENDING");
//        withdrawal.setUser(user);
//        withdrawRequestRepository.save(withdrawal);
//
//        redirectAttributes.addFlashAttribute("message", "Bạn đã yêu cầu rút tiền thành công !");
//        redirectAttributes.addFlashAttribute("withdrawalRequest", new Withdrawal());
//        return "user/withdraw";
    }
    }

