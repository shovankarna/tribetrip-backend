package com.triptribe.expenseservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "expense_history")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String expenseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActionType action;

    @Column(nullable = false)
    private String changedBy;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(columnDefinition = "TEXT")
    private String snapshot; // JSON representation of the expense state

    public enum ActionType {
        CREATED,
        UPDATED,
        DELETED
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}
