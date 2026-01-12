package com.triptribe.expenseservice.repository;

import com.triptribe.expenseservice.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    List<Expense> findByTripId(String tripId);
}
