package com.triptribe.expenseservice.dto;

import com.triptribe.expenseservice.entity.ExpenseSplitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {
    private String id;
    private String tripId;
    private String title;
    private String description;
    private BigDecimal totalAmount;
    private String currency;
    private LocalDate expenseDate;
    private ExpenseSplitType splitType;
    private String category;
    private String createdByUserId;
    private Instant createdAt;
    private List<ExpenseParticipantDto> participants;
}
