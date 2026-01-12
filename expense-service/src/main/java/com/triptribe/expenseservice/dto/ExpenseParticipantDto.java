package com.triptribe.expenseservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseParticipantDto {
    private String userId;
    private BigDecimal amountPaid;
    private BigDecimal amountOwed;
}
