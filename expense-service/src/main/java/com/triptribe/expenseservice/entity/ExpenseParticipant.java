package com.triptribe.expenseservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "expense_participants", indexes = {
        @Index(name = "idx_expense_participant_expense_id", columnList = "expenseId"),
        @Index(name = "idx_expense_participant_user_id", columnList = "userId")
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expenseId;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal amountOwed = BigDecimal.ZERO;
}
