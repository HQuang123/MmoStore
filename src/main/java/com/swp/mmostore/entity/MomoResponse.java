package com.swp.mmostore.entity;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomoResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String message;
    private int resultCode;
    private String payUrl;
    private String deeplink;
    private String qrCodeUrl;
    private String requestType;
}
