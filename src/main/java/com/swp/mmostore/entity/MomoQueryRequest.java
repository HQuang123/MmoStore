package com.swp.mmostore.entity;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomoQueryRequest {
    private String partnerCode;
    private String requestId;
    private String orderId;
    private String signature;
    private String lang = "vi"; // MoMo requires this
}
