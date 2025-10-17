package com.swp.mmostore.entity;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomoRequest {
    private String partnerCode;
    private String clientId;
    private String ipnUrl;
    private String orderId;
    private long amount;
    private String orderInfo;
    private String requestId;
    private String redirectUrl;
    private String lang;
    private String extraData;
    private String signature;
    private String requestType;
}
