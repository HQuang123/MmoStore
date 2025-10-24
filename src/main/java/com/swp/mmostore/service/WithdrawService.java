package com.swp.mmostore.service;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawRequestRepository;
import com.swp.mmostore.util.EmailTemplate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawService {
    private final WithdrawRequestRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;


    //approve a withdrawl request
    @Transactional
    public Withdrawal approveWithdrawal(Integer id) {
        Withdrawal wd = withdrawalRepository.findById(id).orElseThrow();
        wd.setStatus("APPROVED");
        Withdrawal result = withdrawalRepository.save(wd);
        // Gửi email bất đồng bộ cho seller
        String userName = wd.getUser() != null ? wd.getUser().getName()  : "";
        String email = wd.getUser() != null ? wd.getUser().getEmail() : null;
        String amount = wd.getAmount() != null ? wd.getAmount().toString() : "";
        String bankInfo = wd.getBank() + " - " + wd.getBankAccount();
        String approveDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
        String proofFile = "";
        if(email != null && !email.isBlank()){
            String subject = "[MMOStore] Yêu cầu rút tiền đã được duyệt";
            String html = EmailTemplate.withdrawalApprovedEmail(userName, amount, bankInfo, approveDate, proofFile);
            log.info("Send email to {} with subject {}", email, subject);
            emailService.sendEmail(email, subject, html );
        }else{
            log.warn("Seller for withdrawal id={} has no email configured, skipping notification", id);
        }
        return result;
    }

    //reject a withdrawl request
    public Withdrawal rejectWithdrawal(Integer id) {
        Withdrawal wd = withdrawalRepository.findById(id).orElseThrow();
        wd.setStatus("REJECTED");
        Withdrawal result = withdrawalRepository.save(wd);
        //send user email about their rejection
        String userName = wd.getUser() != null ? wd.getUser().getName()  : "";
        String email = wd.getUser() != null ? wd.getUser().getEmail() : null;
        String amount = wd.getAmount() != null ? wd.getAmount().toString() : "";
        String bankInfo = wd.getBank() + " - " + wd.getBankAccount();
        String rejectDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
        String reason = "";
        if(email != null && !email.isBlank()){
            String subject = "[MMOStore] Yêu cầu rút tiền đã bị từ chối";
            String html = EmailTemplate.withdrawalRejectedEmail(userName, amount, bankInfo, rejectDate, reason);
            log.info("Send email to {} with subject {}", email, subject);
            emailService.sendEmail(email, subject, html );
        }
        else{
            log.warn("Seller for withdrawal id={} has no email configured, skipping notification", id);
        }
        return result;
    }

    //processWithdrawl
    @Transactional
    public Withdrawal processWithdrawal(Integer id, String status, String reason, boolean refund) {
        Withdrawal wd = withdrawalRepository.findById(id).orElseThrow();
        if("Approved".equalsIgnoreCase(status)){
            wd.setStatus("Approved");
            wd.setUpdateAt(LocalDateTime.now());
            withdrawalRepository.save(wd);
            String userName = wd.getUser() != null ? wd.getUser().getName()  : "";
            String email = wd.getUser() != null ? wd.getUser().getEmail() : null;
            String amount = wd.getAmount() != null ? wd.getAmount().toString() : "";
            String bankInfo = wd.getBank() + " - " + wd.getBankAccount();
            String approveDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
            String proofFile = "";
            if(email != null && !email.isBlank()){
                String subject = "[MMOStore] Yêu cầu rút tiền đã được duyệt";
                String html = EmailTemplate.withdrawalApprovedEmail(userName, amount, bankInfo, approveDate, proofFile);
                log.info("Send email to {} with subject {}", email, subject);
                emailService.sendEmail(email, subject, html );
            }else{
                log.warn("Seller for withdrawal id={} has no email configured, skipping notification", id);
            }
        }
        //will deduct 1000 dong for each rejected withdrawl request
        else if ("Rejected".equalsIgnoreCase(status)){
            wd.setStatus("Rejected");
            wd.setUpdateAt(LocalDateTime.now());
            withdrawalRepository.save(wd);
            if(refund && wd.getUser() != null && wd.getUser().getUserId() != null){
                BigDecimal amount = wd.getAmount();
                //check if user has enought balance to deduct before
                BigDecimal refundAmount = amount.subtract(new BigDecimal(1000));
                if(refundAmount.compareTo(new BigDecimal(0)) > 0){
                    User user = wd.getUser();
                    user.setBalance(user.getBalance().add(refundAmount));
                    userRepository.save(user);
                }
            }
            //resend email
            String userName = wd.getUser() != null ? wd.getUser().getName()  : "";
            String email = wd.getUser() != null ? wd.getUser().getEmail() : null;
            String amount = wd.getAmount() != null ? wd.getAmount().toString() : "";
            String bankInfo = wd.getBank() + " - " + wd.getBankAccount();
            String rejectDate = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date());
            if(email != null && !email.isBlank()){
                String subject = "[MMOStore] Yêu cầu rút tiền đã bị từ chối";
                String html = EmailTemplate.withdrawalRejectedEmail(userName, amount, bankInfo, rejectDate, reason);
                log.info("Send email to {} with subject {}", email, subject);
                emailService.sendEmail(email, subject, html );
            }
            else{
                log.warn("Seller for withdrawal id={} has no email configured, skipping notification", id);
            }
        }
        return wd;
    }


    public List<Withdrawal> getWithdrawalHistoryForUser(User user) {
        // Call the new method that automatically sorts the results
        return withdrawalRepository.findByUserOrderByCreateAtDesc(user);
    }

}
