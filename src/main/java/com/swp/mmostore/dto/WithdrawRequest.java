package com.swp.mmostore.dto;

import com.swp.mmostore.entity.User;
import com.swp.mmostore.entity.Withdrawal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawRequest {
    private Integer userId;
    private Integer withdrawalId;
}
