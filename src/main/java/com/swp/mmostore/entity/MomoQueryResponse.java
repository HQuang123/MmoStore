package com.swp.mmostore.entity;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomoQueryResponse {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private String amount;
    private String message;
    private String resultCode; // MoMo sends this as a String (e.g., "0")
}
