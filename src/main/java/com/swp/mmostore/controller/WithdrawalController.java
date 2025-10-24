package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Bank;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawRequestRepository;
import com.swp.mmostore.service.EmailService;
import com.swp.mmostore.service.NotificationService;
import com.swp.mmostore.service.WithdrawService;
import com.swp.mmostore.util.EmailTemplate;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WithdrawalController {
    private final UserRepository userRepository;
    private final WithdrawRequestRepository withdrawRequestRepository;
    private final WithdrawService withdrawService;
    private final EmailService emailService;
    private final NotificationService notificationService;

    @ModelAttribute //this method will automatically model.addAttribute for every method in this class
    public void addCommonAttributes(Model model) {
        String email = MockSecurityUtils.getCurrentUserEmail();
        log.info("Current user id: {}", email);
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/wallet/withdraw")
    public String showWithdrawMoneyPage(Model model, RedirectAttributes redirectAttributes) {
        model.addAttribute("banks", Bank.listAll());
        model.addAttribute("withdraw", new Withdrawal());
        //load history of withdrawl of user
        User user = (User) model.getAttribute("user");
        List<Withdrawal> withdrawals = withdrawService.getWithdrawalHistoryForUser(user);
        model.addAttribute("withdrawals", withdrawals);
        return "user/withdraw";
    }

    @PostMapping("/user/wallet/withdraw")
    @Transactional
    public String createWithdrawlForm(@ModelAttribute Withdrawal withdrawal, RedirectAttributes redirectAttributes, BindingResult result , Model model) {
        try {
            User user = (User) model.getAttribute("user");
            if (result.hasErrors()) {
                return "user/withdraw";
            }
            if (withdrawal.getAmount().compareTo(user.getBalance()) > 0) {
                redirectAttributes.addFlashAttribute("errorMessage", "Số dư không đủ");
                return "redirect:/user/wallet/withdraw"; //return to @GET MAPPing
            }
            withdrawal.setStatus("Pending");
            //Deduct user balance
            user.setBalance(user.getBalance().subtract(withdrawal.getAmount()));
            userRepository.save(user);
            withdrawal.setUser(user);
            withdrawRequestRepository.save(withdrawal);
            //send email to user
            String subject = "[MMOStore] Đã nộp đơn rút tiền";
            String content = EmailTemplate.withdrawalRequestEmail(user.getName(),  withdrawal.getAmount().toString(), withdrawal.getBank().getDisplayName() + "-" + withdrawal.getBankAccount(), new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));

            emailService.sendEmailAsync(user.getEmail(), subject, content);
            //
            notificationService.createNotificationForUser(user.getUserId(), "Yêu cầu rút tiền", "Yêu cầu rút " + withdrawal.getAmount() + " VND đã được gửi và đang chờ duyệt !");
            try {
                notificationService.createNotificationForRole(
                        "ROLE_ADMIN",
                        "Yêu cầu rút tiền của người dùng đang chờ",
                        "New withdrawal request of " +  withdrawal.getAmount().toString() + " by " + user.getEmail() + "  pending approval."
                );
            } catch (Exception ignored) {
            }

            redirectAttributes.addFlashAttribute("successMessage", "Yêu cầu rút tiền nộp thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi server: " + ex.getMessage());
            return "redirect:/user/wallet/withdraw";
        }
        return "redirect:/user/wallet/withdraw";
    }
    }

