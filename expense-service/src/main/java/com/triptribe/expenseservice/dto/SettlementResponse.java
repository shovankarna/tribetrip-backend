package com.triptribe.expenseservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class SettlementResponse {
    private String fromUserId; // Debtor
    private String toUserId; // Creditor
    private BigDecimal amount;
    private String currency;
}
