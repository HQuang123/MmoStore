package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Bank;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawRequestRepository;
import com.swp.mmostore.util.MockSecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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


    @GetMapping("/user/withdraw")
    public String showWithdrawForm(Model model){
        Withdrawal withdrawalRequest = (Withdrawal) model.getAttribute("withdrawalRequest");
        if(withdrawalRequest == null){
            withdrawalRequest = new Withdrawal();
        }
        model.addAttribute("withdrawalRequest", withdrawalRequest);
        model.addAttribute("banks", Bank.listAll());
        withdrawalRequest.setStatus("PENDING");
        return "user/withdraw";
    }

    //
    @PostMapping("/user/withdraw")
    public String submitWithdrawal(@ModelAttribute Withdrawal withdrawal, RedirectAttributes redirectAttributes) {
        withdrawRequestRepository.save(withdrawal);
        redirectAttributes.addFlashAttribute("withdrawalRequest", withdrawal);
        redirectAttributes.addFlashAttribute("message", "Withdrawal request submitted successfully!");
        return "redirect:/user/withdraw";
    }
}
