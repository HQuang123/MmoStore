package com.swp.mmostore.service;

import com.swp.mmostore.entity.Deposit;
import com.swp.mmostore.entity.Order;
import org.springframework.beans.factory.annotation.Value;
import com.swp.mmostore.entity.MomoRequest;
import com.swp.mmostore.entity.MomoResponse;
import com.swp.mmostore.repository.MomoAPI;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
//lombok annotation to provide a logger
public class MomoService {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.redirect_url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;

    @Value("${momo.request-type}")
    private String requestType;

    public final MomoAPI momoAPI;

    //create momo request from order, then return the response
    public MomoResponse createQr(Deposit deposit){
        String orderId = deposit.getId().toString();
        String orderInfo = "Thanh toan don hang " + orderId;
        String requestId = UUID.randomUUID().toString();
        String extraData = "Khong co khuyen mai gi het";
        long amount = deposit.getAmount().longValue();
        String rawSignature = String.format("accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s", accessKey, amount, extraData, ipnUrl, orderId, orderInfo, partnerCode, redirectUrl, requestId, requestType);
        log.info(">>>>>endpoint: {}", endpoint);
        log.info(">>>>>rediret_url: {}", redirectUrl);
        String prettySignature = "";
        //catch exception
        try{
            prettySignature = signHmacSHA256(rawSignature, secretKey); //same as certificate from the user --> user has to sign the request, momo will have the same key to verify the signature
        }catch (Exception e){
            log.error(">>>>>Co loi khi hash code: {}", e.getMessage());
            return null;
        }

        if(prettySignature == null || prettySignature.isBlank()){
            log.error(">>>>>Signature is null or blank");
            return null;
        }

        MomoRequest request = MomoRequest.builder().
                partnerCode(partnerCode).
                requestType(requestType).
                ipnUrl(ipnUrl).
                redirectUrl(redirectUrl)
                .orderId(orderId)
                .orderInfo(orderInfo)
                .requestId(requestId)
                .extraData(extraData)
                .amount(amount)
                .signature(prettySignature)
                .lang("vi").
                build();
        return momoAPI.createMomoQR(request);
    }

    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKeySpec);
        byte[] bytes = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(0xff & aByte);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
