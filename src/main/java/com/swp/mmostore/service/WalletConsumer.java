package com.swp.mmostore.service;

import com.swp.mmostore.dto.OrderEvent;
import com.swp.mmostore.dto.WalletTransactionEvent;
import com.swp.mmostore.dto.WithdrawRequest;
import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.Order;
import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.repository.WithdrawalRepository;
import com.swp.mmostore.util.EmailTemplate;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WalletConsumer {

    private final UserRepository userRepository;
    private final WithdrawalRepository withdrawalRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final WithdrawService withdrawService;
    private final DepositRepository depositRepository;
    private final OrderRepository orderRepository;
    private final OrderConsumer orderConsumer;
    private final OrderProducer orderProducer;
    private final EmailTemplate emailTemplate;

    @KafkaListener(topics = "wallet-transaction", groupId = "wallet-service")
    @Transactional
    public void processWalletTransaction(WalletTransactionEvent event) throws MessagingException {
        System.out.println("Wallet transaction event received: " + event.getTransactionId() + " of type: " + event.getType());
        switch (event.getType()) {
            case Withdraw:
                processWithdraw(event);
                break;

            case Top_up:
                processTopUp(event);
                break;

            case Order_payment:
                processOrderPayment(event);
                break;
            case Order_refund:
                processOrderRefund(event);
                break;
        }
    }

    private void processWithdraw(WalletTransactionEvent event) throws MessagingException {
        //process withdraw
        Withdrawal wd = withdrawalRepository.findById(event.getTransactionId()).orElseThrow();
        User user = wd.getUser();
        //release on-hold balance
//        user.setOnHoldBalance(user.getOnHoldBalance().subtract(event.getAmount()));

        if ("Approved".equalsIgnoreCase(event.getStatus())) {
            withdrawService.approveWithdrawal(wd);
        } else if ("Rejected".equalsIgnoreCase(event.getStatus())) {
            //refund to user's wallet
            user.setBalance(user.getBalance().add(event.getAmount()));
            withdrawService.rejectWithdrawal(wd);
        }
        userRepository.save(user);

    }

    private void processTopUp(WalletTransactionEvent event) {
        Deposit deposit = depositRepository.findById(event.getTransactionId()).orElseThrow();
        User user = deposit.getUser();
        user.setBalance(user.getBalance().add(deposit.getAmount())); //likely to cause error due to
        userRepository.save(user);
        depositRepository.save(deposit);
    }

    private void processOrderPayment(WalletTransactionEvent event) {
        Order order = orderRepository.findById(event.getTransactionId()).orElseThrow();
        User user = order.getUser();

        if (user.getBalance().compareTo(order.getTotalPrice()) < 0) {
            order.setStatus("Failed");
            orderRepository.save(order);
            throw new RuntimeException("Insufficient balance for user id: " + user.getUserId());
        }

        user.setBalance(user.getBalance().subtract(order.getTotalPrice()));
        order.setStatus("Payed");
        orderRepository.save(order);
        userRepository.save(user);

        // send order event to order consumer to process order fulfillment
        OrderEvent orderEvent = new OrderEvent();
        orderEvent.setOrderId(order.getOrderId());
        orderEvent.setUserId(user.getUserId());
        orderEvent.setProductId(order.getProduct().getProductId());
        orderProducer.sendNewOrder(orderEvent);
    }

    private void processOrderRefund(WalletTransactionEvent event) {
        Order order = orderRepository.findById(event.getTransactionId()).orElseThrow();
        User user = order.getUser();
        user.setBalance(user.getBalance().add(order.getTotalPrice())); //likely to cause error due to
        order.setStatus("Refunded");
        userRepository.save(user);
        orderRepository.save(order);
    }

    @KafkaListener(topics = "withdraw-request", groupId = "withdraw-service")
    @Transactional
    public void processWithdrawRequest(WithdrawRequest request) {
        User user = userRepository.findById(request.getUserId()).orElseThrow();
        Withdrawal withdrawal = withdrawalRepository.findById(request.getWithdrawalId()).orElseThrow();

        if (user.getBalance().compareTo(withdrawal.getAmount() ) >= 0) {
            user.setBalance(user.getBalance().subtract(withdrawal.getAmount()));
//            user.setOnHoldBalance(user.getOnHoldBalance().add(withdrawal.getAmount()));
            userRepository.save(user);
            withdrawal.setStatus("Pending");
            withdrawalRepository.save(withdrawal);

            String subject = "[MMOStore] Đã nộp đơn rút tiền";
            String content = emailTemplate.withdrawalRequestEmail(user.getName(), withdrawal.getAmount().toString(), withdrawal.getBank().getDisplayName() + "-" + withdrawal.getBankAccount(), new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
            emailService.sendEmailAsync(user.getEmail(), subject, content);
            //
            notificationService.createNotificationForUser(user.getUserId(), "Yêu cầu rút tiền", "Yêu cầu rút " + withdrawal.getAmount() + " VND đã được gửi và đang chờ duyệt !");

            try {
                notificationService.createNotificationForRole("ROLE_ADMIN", "Yêu cầu rút tiền của người dùng đang chờ", "New withdrawal request of " + withdrawal.getAmount().toString() + " by " + user.getEmail() + "  pending approval.");
            } catch (Exception ignored) {

            }
        } else {
            // Handle insufficient balance case
            notificationService.createNotificationForUser(user.getUserId(), "Yêu cầu rút tiền", "Yêu cầu rút " + withdrawal.getAmount() + " VND thất bại do số dư không đủ.");

        }
    }
}
