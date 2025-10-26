package com.swp.mmostore.controller;

import com.swp.mmostore.entity.Bank;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawalRepository;
import com.swp.mmostore.service.EmailService;
import com.swp.mmostore.service.NotificationService;
import com.swp.mmostore.service.WithdrawService;
import com.swp.mmostore.util.EmailTemplate;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WithdrawalController {
    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
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
//            user.setBalance(user.getBalance().subtract(withdrawal.getAmount()));
            userRepository.save(user);
            withdrawal.setUser(user);
            withdrawalRepository.save(withdrawal);
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

    @GetMapping("/admin/withdraw")
    public String getWithdrawManagementPage(@RequestParam(value="keyword", required = false) String keyword,
                                            @RequestParam(value="status", required = false) String status,
                                            @RequestParam(value = "page", defaultValue = "1") int page,  Model model) {
        if(page < 1){
            page = 1;
        }
        //create a pageable object
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createAt").descending());
        Page<Withdrawal> withdrawalPage = withdrawService.findWithdrawals(keyword,status, pageable);
        model.addAttribute("withdrawPage", withdrawalPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "admin/withdraw-management";
        }

        @GetMapping("/admin/withdraw/generate-qr/{id}")
        @ResponseBody
        public ResponseEntity<?> getWithdrawDetail(@PathVariable("id") Integer id) {
            Withdrawal wd = withdrawalRepository.findById(id).orElse(null);
            if (wd == null) {
                return ResponseEntity.badRequest().body("Withdrawal request not found");
            }
            if (!"Pending".equalsIgnoreCase(wd.getStatus())) {
                return ResponseEntity.status(400).body(Map.of("error", "Request is not in Pending state."));
            }

            String vietQrUrl = null;
            try{
                String bankName = wd.getBank().getDisplayName();
                String accountNumber = wd.getBankAccount();
                String userName = wd.getUser().getName();
                String code = Bank.findCodeForBankName(bankName);
                if (code != null && accountNumber != null && !accountNumber.isBlank()) {
                    String token = "jYp8Yod"; // Your placeholder token
                    String filename = code + "-" + accountNumber + "-" + token + ".jpg";
                    String accountName = URLEncoder.encode(userName == null ? "" : userName, StandardCharsets.UTF_8);
                    String addInfo = URLEncoder.encode((wd.getId() != null ? ("WD#" + wd.getId()) : ("WD:" + accountNumber)), StandardCharsets.UTF_8);

                    // Don't forget the amount!
                    String amount = String.valueOf(wd.getAmount().longValue());

                    vietQrUrl = "https://img.vietqr.io/image/" + filename
                            + "?accountName=" + accountName
                            + "&addInfo=" + addInfo
                            + "&amount=" + amount; // Added amount, as it's critical
                }
                if (vietQrUrl == null) {
                    return ResponseEntity.status(500).body(Map.of("error", "Could not generate QR URL. Check bank info."));
                }
                return ResponseEntity.ok(Map.of("vietQrUrl", vietQrUrl));
            } catch (Exception e) {
                return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
            }
        }

    @PostMapping("/admin/withdraw/confirm-approval/{id}")
    @ResponseBody
    public ResponseEntity<?> confirmWithdrawal(@PathVariable("id") Integer id, Model model) {
        Withdrawal wd = withdrawalRepository.findById(id).orElse(null);
        if (wd == null) {
            return ResponseEntity.badRequest().body("Withdrawal request not found");
        }
        if (!"Pending".equalsIgnoreCase(wd.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "Request is not in Pending state."));
        }
        try{
            User user = (User) model.getAttribute("user");
            //ToDo: add quueu here
            user.setBalance(user.getBalance().subtract(wd.getAmount()));
            wd.setStatus("Approved");
            withdrawalRepository.save(wd);

            return ResponseEntity.ok(Map.of(
                    "message", "Withdrawal approved successfully.",
                    "newStatus", "Approved"
            ));
        }
        catch(Exception e){
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    }

