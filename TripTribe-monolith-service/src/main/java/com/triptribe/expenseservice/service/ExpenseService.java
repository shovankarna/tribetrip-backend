package com.triptribe.expenseservice.service;

import com.triptribe.expenseservice.dto.BalanceResponse;
import com.triptribe.expenseservice.dto.CreateExpenseRequest;
import com.triptribe.expenseservice.dto.ExpenseResponse;
import com.triptribe.expenseservice.dto.SettlementResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(CreateExpenseRequest request, String userId);

    List<ExpenseResponse> getTripExpenses(String tripId, String userId);

    List<BalanceResponse> getBalances(String tripId, String userId);

    List<SettlementResponse> getSettlements(String tripId, String userId);

    void deleteExpense(String expenseId, String userId);
}
