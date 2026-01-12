package com.triptribe.expenseservice.dto;

import com.triptribe.expenseservice.entity.ExpenseSplitType;
import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateExpenseRequest {

    @NotBlank(message = "Trip ID is mandatory")
    private String tripId;

    @NotBlank(message = "Title is mandatory")
    private String title;

    private String description;

    @NotNull(message = "Amount is mandatory")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal totalAmount;

    @NotBlank(message = "Currency is mandatory")
    private String currency;

    @NotNull(message = "Date is mandatory")
    private LocalDate expenseDate;

    @NotNull(message = "Split type is mandatory")
    private ExpenseSplitType splitType;

    private String category;

    // List of user IDs for simple splits (EQUAL)
    // Or detailed participant info for EXACT/PERCENTAGE can be handled via a
    // complex list object
    // For MVP strict adherence to blueprint:
    // "payers[]", "owers[]"

    private List<ExpenseParticipantDto> participants;
}
