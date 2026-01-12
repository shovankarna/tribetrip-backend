package com.triptribe.expenseservice.repository;

import com.triptribe.expenseservice.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {
    List<ExpenseParticipant> findByExpenseId(String expenseId);

    List<ExpenseParticipant> findByUserId(String userId);

    void deleteByExpenseId(String expenseId);
}
