package com.triptribe.expenseservice.repository;

import com.triptribe.expenseservice.entity.ExpenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseHistoryRepository extends JpaRepository<ExpenseHistory, Long> {
    List<ExpenseHistory> findByExpenseId(String expenseId);
}
