package com.swp.mmostore.service;

import com.swp.mmostore.entity.Bank;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawalRepository;
import com.swp.mmostore.util.EmailTemplate;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawService {
    private final WithdrawalRepository withdrawalRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public List<Withdrawal> getWithdrawalHistoryForUser(User user, Pageable pageable) {
        // Just call the new repository method
        return withdrawalRepository.findByUserAndStatusNot(user, "UNCONFIRMED", Sort.by(Sort.Direction.DESC, "createAt"));
    }
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
        return withdrawalRepository.findByUserAndStatusNot(user, "Unconfirmed", Sort.by(Sort.Direction.DESC, "createAt"));
    }

    public Page<Withdrawal> findWithdrawals(String keyword, String status, Pageable pageable) {

        // giong query builder cho sql query)
        Specification<Withdrawal> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Filter by keyword in user's name
            if (keyword != null && !keyword.isBlank()) {
                // Assumes Withdraw has a 'user' relationship, and User has a 'username' or 'fullName' field
                predicates.add(cb.like(cb.lower(root.get("user").get("name")), "%" + keyword.toLowerCase() + "%"));
            }

            // 2. Filter by status
            if (status != null && !status.isBlank()) {
                try {
                    predicates.add(cb.equal(root.get("status"), status));
                } catch (IllegalArgumentException e) {
                    // Handle invalid status string if necessary
                }
            }
            else{
                predicates.add(cb.notEqual(root.get("status"), "Unconfirmed"));
            }

            // Combine all predicates with AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // Execute the query with the dynamic specification and pagination
        return withdrawalRepository.findAll(spec, pageable);
    }

    @Transactional
    public Withdrawal chargeRegistrationFee(User user, BigDecimal amount, String note) {
        // Kiểm tra số dư người dùng
        if (user.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Số dư không đủ để đăng ký cửa hàng. Vui lòng nạp thêm tiền vào ví.");
        }

        // Trừ tiền
        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);

        // Tạo bản ghi Withdrawal cho phí đăng ký
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setUser(user);
        withdrawal.setAmount(amount);
        withdrawal.setBank(Bank.SYSTEM); // thêm enum SYSTEM vào enum Bank (phí nội bộ)
        withdrawal.setBankAccount("REGISTER_FEE");
        withdrawal.setAccountHolder("MMOStore System");
        withdrawal.setStatus("APPROVED");
        withdrawal.setCreateAt(LocalDateTime.now());
        withdrawal.setUpdateAt(LocalDateTime.now());

        // Lưu bản ghi
        withdrawalRepository.save(withdrawal);

        log.info("Đã trừ {}đ từ tài khoản {} cho phí đăng ký shop", amount, user.getEmail());
        return withdrawal;
    }


}
