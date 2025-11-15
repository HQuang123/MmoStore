package com.swp.mmostore.controller;

import com.swp.mmostore.dto.WalletTransactionEvent;
import com.swp.mmostore.entity.*;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawalOtpRepository;
import com.swp.mmostore.repository.WithdrawalRepository;
import com.swp.mmostore.service.EmailService;
import com.swp.mmostore.service.NotificationService;
import com.swp.mmostore.service.WalletProducer;
import com.swp.mmostore.service.WithdrawService;
import com.swp.mmostore.util.EmailTemplate;
import com.swp.mmostore.util.MockSecurityUtils;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class WithdrawalController {
    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final WithdrawService withdrawService;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final WithdrawalOtpRepository withdrawalOtpRepository;
    private final WalletProducer walletProducer;
    private final EmailTemplate emailTemplate;

    private String generateOtp() {
        return String.format("%06d", new java.util.Random().nextInt(1_000_000));
    }

    @ModelAttribute //this method will automatically model.addAttribute for every method in this class
    public void addCommonAttributes(Model model) {
        String email = MockSecurityUtils.getCurrentUserEmail();
        log.info("Current user id: {}", email);
        User user = userRepository.findByEmail(email);
        model.addAttribute("user", user);
    }

    @GetMapping("/user/wallet/withdraw")
    public String showWithdrawMoneyPage(Model model,
                                        RedirectAttributes redirectAttributes,
                                        // 1. Add Pageable parameter
                                        @PageableDefault(size = 10, sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {

        model.addAttribute("banks", Bank.listAll());
        model.addAttribute("withdrawal", new Withdrawal());

        User user = (User) model.getAttribute("user");

        // Call the original service method
        List<Withdrawal> withdrawals = withdrawService.getWithdrawalHistoryForUser(user);

        // Add the List back to the model (not a Page)
        model.addAttribute("withdrawals", withdrawals);

        return "user/withdraw";
    }

    @PostMapping("/user/wallet/withdraw")
    @Transactional
    public String createWithdrawlForm(@Valid @ModelAttribute Withdrawal withdrawal, BindingResult result, RedirectAttributes redirectAttributes, Model model) {
        try {
            User user = (User) model.getAttribute("user");
            if (result.hasErrors()) {
                model.addAttribute("banks", Bank.listAll());
                List<Withdrawal> withdrawals = withdrawService.getWithdrawalHistoryForUser(user);
                model.addAttribute("withdrawals", withdrawals);
                return "user/withdraw";
            }
            //compare amount to withdraw with available balance (real balance - onHold balance)
            if (withdrawal.getAmount().compareTo(user.getBalance()) > 0) {
                redirectAttributes.addFlashAttribute("errorMsg", "Số dư không đủ");
                return "redirect:/user/wallet/withdraw"; //return to @GET MAPPing
            }

            withdrawal.setStatus("Unconfirmed");
            withdrawal.setUser(user);
            withdrawalRepository.save(withdrawal);
            String token = generateOtp();
            WithdrawalOtp otp = new WithdrawalOtp(token, withdrawal);
            withdrawalOtpRepository.save(otp);
            String subject = "[MMOStore] Mã OTP Xác Nhận Rút Tiền";
            String content = "Mã OTP của bạn là: " + token + ". Mã này sẽ hết hạn trong vòng 5 phút";
            emailService.sendEmailAsync(user.getEmail(), subject, content);
            redirectAttributes.addFlashAttribute("withdrawal", withdrawal);
            return "redirect:/user/wallet/withdraw/confirm";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi Server !");
            return "redirect:/user/wallet/withdraw";
        }
    }

    @GetMapping("/user/wallet/withdraw/confirm")
    public String showConfirmWithdrawPage() {
        return "user/withdrawal-confirm";
    }

    @PostMapping("/user/wallet/withdraw/confirm")
    @Transactional
    public String processWithdrawalConfirm(@RequestParam("withdrawalID") Integer withdrawalId, @RequestParam("otp") String otp, RedirectAttributes redirectAttributes, Model model) {
        try {
            User user = (User) model.getAttribute("user");
            if (withdrawalId == null) {
                throw new RuntimeException("Withdrawal request not found");
            }
            Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId).orElse(null);
            WithdrawalOtp withdrawalOtp = withdrawalOtpRepository.findByTokenAndWithdrawal(otp, withdrawal);
            if (withdrawalOtp == null || withdrawalOtp.isExpired()) {
                redirectAttributes.addFlashAttribute("withdrawal", withdrawal);
                redirectAttributes.addFlashAttribute("errorMsg", "Mã OTP không hợp lệ hoặc hết hạn");
                return "redirect:/user/wallet/withdraw/confirm";
            }
            //TODO: Implement MQ for withdrawal @ShiroHoang

            // Add request to Kafka queue for processing
            walletProducer.sendWithdrawRequest(user, withdrawal);

            redirectAttributes.addFlashAttribute("successMsg", "Yêu cầu rút tiền nộp thành công.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi server: " + ex.getMessage());
            return "redirect:/user/wallet/withdraw";
        }
        return "redirect:/user/wallet";
    }

    @PostMapping("/user/wallet/withdraw/resend-otp")
    @Transactional
    public String resendWithdrawlOtp(@RequestParam("withdrawalID") Integer withdrawalId, RedirectAttributes redirectAttributes, Model model) {
        try {
            User user = (User) model.getAttribute("user");
            Withdrawal withdrawal = withdrawalRepository.findById(withdrawalId).orElseThrow(() -> new RuntimeException("Yêu cầu rút tiền không tìm thấy"));
            if (withdrawal.getUser().getUserId() != user.getUserId()) {
                redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: Yêu cầu không hợp lệ.");
                return "redirect:/user/wallet/withdraw";
            }
            //2. check if unconfirmed
            if (!"Unconfirmed".equalsIgnoreCase(withdrawal.getStatus())) {
                redirectAttributes.addFlashAttribute("errorMsg", "Yêu cầu này đã được xử lý hoặc hủy bỏ.");
                return "redirect:/user/wallet/withdraw";
            }
            // --- Resend Logic ---
            // 1. Delete the old token
            WithdrawalOtp oldOtp = withdrawalOtpRepository.findByWithdrawal(withdrawal);
            if (oldOtp != null) {
                withdrawalOtpRepository.delete(oldOtp);
                withdrawalOtpRepository.flush(); // Force the delete to happen NOW
            }

            // 2. Create and save a new OTP
            String token = generateOtp();
            WithdrawalOtp newOtp = new WithdrawalOtp(token, withdrawal);
            withdrawalOtpRepository.save(newOtp);

            // 3. Send the new email
            String subject = "[MMOStore] Mã OTP Xác nhận Rút tiền (Gửi lại)";
            String content = "Mã OTP mới của bạn là: " + token + ". Mã này sẽ hết hạn trong 5 phút.";
            emailService.sendEmailAsync(user.getEmail(), subject, content); // Assuming you use sendEmailAsync

            // 4. Redirect back to the confirm page with a success message
            redirectAttributes.addFlashAttribute("successMsg", "Đã gửi lại mã OTP. Vui lòng kiểm tra email.");
            redirectAttributes.addFlashAttribute("withdrawal", withdrawal); // Pass the ID back
            return "redirect:/user/wallet/withdraw/confirm";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi server: " + e.getMessage());
            return "redirect:/user/wallet/withdraw";
        }


    }

    @GetMapping("/admin/withdraw")
    public String getWithdrawManagementPage(@RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "status", required = false) String status, @RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        if (page < 1) {
            page = 1;
        }
        //create a pageable object
        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createAt").descending());
        Page<Withdrawal> withdrawalPage = withdrawService.findWithdrawals(keyword, status, pageable);
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
            return ResponseEntity.badRequest().body("Không tìm thấy yêu cầu rút tiền");
        }
        if (!"Pending".equalsIgnoreCase(wd.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "Yêu cầu không trong trạng thái đúng"));
        }

        String vietQrUrl = null;
        try {
            String bankName = wd.getBank().getDisplayName();
            String accountNumber = wd.getBankAccount();
            String userName = wd.getUser().getName();
            String code = Bank.findCodeForBankName(bankName);
            if (code != null && accountNumber != null && !accountNumber.isBlank()) {
                String filename = code + "-" + accountNumber + "-print" + ".png";
                String accountName = URLEncoder.encode(userName == null ? "" : userName, StandardCharsets.UTF_8);
                String addInfo = URLEncoder.encode((wd.getId() != null ? ("WD#" + wd.getId()) : ("WD:" + accountNumber)), StandardCharsets.UTF_8);

                // Don't forget the amount!
                String amount = String.valueOf(wd.getAmount().longValue());

                vietQrUrl = "https://img.vietqr.io/image/" + filename + "?accountName=" + accountName + "&addInfo=" + addInfo + "&amount=" + amount; // Added amount, as it's critical
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
            return ResponseEntity.badRequest().body("Không tìm thấy yêu cầu rút tiền");
        }
        if (!"Pending".equalsIgnoreCase(wd.getStatus())) {
            return ResponseEntity.status(400).body(Map.of("error", "Yêu cầu không trong trạng thái đúng"));
        }
        try {
            User user = (User) model.getAttribute("user");

            //ToDo: add quueu here
            WalletTransactionEvent event = new WalletTransactionEvent();
            event.setTransactionId(wd.getId());
            event.setStatus("Approved");
            event.setType(com.swp.mmostore.entity.ActionType.Withdraw);
            walletProducer.sendTransactionEvent(event);

            // add notification for successful approval for user
            if(user != null) {
                notificationService.createNotificationForUser(
                        user.getUserId(), "Kết quả rút tiền", "Yêu cầu rút " + wd.getAmount() + " đã được duyệt !"
                );
            }

            return ResponseEntity.ok(Map.of("message", "Withdrawal approved successfully.", "newStatus", "Approved"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Server error: " + e.getMessage()));
        }
    }

    @PostMapping("/admin/withdraw/reject")
    public String processWithdrawalReject(@RequestParam("id") Integer withdrawalId, RedirectAttributes redirectAttributes, Model model) {
        try{
            WalletTransactionEvent event = new WalletTransactionEvent();
            event.setTransactionId(withdrawalId);
            event.setStatus("Rejected");
            event.setType(ActionType.Withdraw);
            walletProducer.sendTransactionEvent(event);
            redirectAttributes.addFlashAttribute("successMsg","Withdrawal #" + withdrawalId + " has been rejected");
            redirectAttributes.addFlashAttribute("successMessage","Withdrawal #" + withdrawalId + " has been rejected");

            // add notification for successful approval for user
            Optional<Withdrawal> withdraw = withdrawalRepository.findById(withdrawalId);
            withdraw.ifPresent(withdrawal -> notificationService.createNotificationForUser(
                    withdrawal.getUser().getUserId(), "Kết quả rút tiền", "Yêu cầu rút " + withdrawal.getAmount() + " đã bị từ chối !")
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg","Server error: " + e.getMessage());
        }
        return "redirect:/admin/withdraw";
    }

}

