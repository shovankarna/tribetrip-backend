package com.triptribe.expenseservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BalanceResponse {
    private String userId;
    private BigDecimal balance; // Positive = Owed to user, Negative = User owes
}
