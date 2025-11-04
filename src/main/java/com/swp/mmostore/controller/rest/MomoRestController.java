package com.swp.mmostore.controller.rest;

import com.swp.mmostore.entity.*;
import com.swp.mmostore.repository.DepositRepository;
import com.swp.mmostore.repository.OrderRepository;
import com.swp.mmostore.repository.UserRepository;
import com.swp.mmostore.service.DepositService;
import com.swp.mmostore.service.MomoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
@Slf4j
@RequiredArgsConstructor
//init final field

@RestController
@RequestMapping("api/momo")
public class MomoRestController {
    private final DepositService depositService;

    private final DepositRepository depositRepository;

    private final UserRepository userRepository;

    private final MomoService momoService;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @PostMapping("create")
    public MomoResponse createQRCode() {
        Deposit deposit = new Deposit();
        deposit.setId(1);
        deposit.setActionType(ActionType.Top_up);
        deposit.setPaymentMethod("Momo");
        deposit.setAmount(BigDecimal.valueOf(100000));
        //generate a QR code
        return momoService.createQr(deposit);
    }
    //0 is success, !0 is failure
    @Transactional
    @PostMapping("/ipn-handler")
    public ResponseEntity<Object> ipnHandler(@RequestBody Map<String, String> payload) {
        try{
            String momoSignature = (String) payload.get("signature");
            if(momoSignature == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Or FORBIDDEN
            }

            String rawSignature = "accessKey=" + accessKey +
                    "&amount=" + payload.get("amount") +
                    "&extraData=" + payload.get("extraData") +
                    "&message=" + payload.get("message") +
                    "&orderId=" + payload.get("orderId") +
                    "&orderInfo=" + payload.get("orderInfo") +
                    "&orderType=" + payload.get("orderType") +
                    "&partnerCode=" + payload.get("partnerCode") +
                    "&payType=" + payload.get("payType")+
                    "&requestId=" + payload.get("requestId") +
                    "&responseTime=" + payload.get("responseTime") +
                    "&resultCode=" + payload.get("resultCode") +
                    "&transId=" + payload.get("transId");
            String reComputeSignature = momoService.signHmacSHA256(rawSignature, secretKey);
            if(!momoSignature.equals(reComputeSignature)) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Or FORBIDDEN
            }
            String orderId = payload.get("orderId");
            String requestId = payload.get("requestId");
            MomoQueryResponse momoQueryResponse = momoService.queryTransactionStatus(orderId, requestId);
            String trustedResultCode = momoQueryResponse.getResultCode();
            log.info("Trusted Result Code: {}", trustedResultCode);
            Integer depositId = Integer.parseInt(payload.get("orderId"));
            Deposit deposit = depositRepository.findById(depositId).orElse(null);

            log.info("Deposit Id la: {}" ,deposit.getId());
            log.info("User id la: {}" ,deposit.getUser().getUserId());
            log.info("User name la: {}" ,deposit.getUser().getName());
            User user = deposit.getUser();

            if(trustedResultCode.equals("0")) {
                deposit.setStatus(DepositStatus.Completed);
                //TODO: implement MQ
                user.setBalance(user.getBalance().add(deposit.getAmount())); //likely to cause error due to
                depositRepository.save(deposit);
                userRepository.save(user);
            }
            else{
                deposit.setStatus(DepositStatus.Failed);
                depositRepository.save(deposit);
            }
            return ResponseEntity.noContent().build();
        }
        catch (Exception e){
            log.error("Loi khi lay request body tu ipn url + {}",e.getMessage());
            return ResponseEntity.noContent().build(); //momo needs to response back with 204 code to avoid spamming to BE api
        }
    }
}