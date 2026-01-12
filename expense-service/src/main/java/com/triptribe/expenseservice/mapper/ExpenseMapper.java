package com.triptribe.expenseservice.mapper;

import com.triptribe.expenseservice.dto.ExpenseParticipantDto;
import com.triptribe.expenseservice.dto.ExpenseResponse;
import com.triptribe.expenseservice.entity.Expense;
import com.triptribe.expenseservice.entity.ExpenseParticipant;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ExpenseMapper {

    public ExpenseResponse toDto(Expense expense, List<ExpenseParticipant> participants) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .tripId(expense.getTripId())
                .title(expense.getTitle())
                .description(expense.getDescription())
                .totalAmount(expense.getTotalAmount())
                .currency(expense.getCurrency())
                .expenseDate(expense.getExpenseDate())
                .splitType(expense.getSplitType())
                .category(expense.getCategory())
                .createdByUserId(expense.getCreatedByUserId())
                .createdAt(expense.getCreatedAt())
                .participants(participants.stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()))
                .build();
    }

    public ExpenseParticipantDto toDto(ExpenseParticipant participant) {
        return ExpenseParticipantDto.builder()
                .userId(participant.getUserId())
                .amountPaid(participant.getAmountPaid())
                .amountOwed(participant.getAmountOwed())
                .build();
    }
}
