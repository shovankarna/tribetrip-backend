package com.triptribe.expenseservice.controller;

import com.triptribe.expenseservice.config.UserContext;
import com.triptribe.expenseservice.dto.*;
import com.triptribe.expenseservice.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(@Valid @RequestBody CreateExpenseRequest request) {
        String userId = UserContext.getUserId();
        return expenseService.createExpense(request, userId);
    }

    @GetMapping
    public List<ExpenseResponse> getTripExpenses(@RequestParam String tripId) {
        String userId = UserContext.getUserId();
        return expenseService.getTripExpenses(tripId, userId);
    }

    @GetMapping("/balances")
    public List<BalanceResponse> getBalances(@RequestParam String tripId) {
        String userId = UserContext.getUserId();
        return expenseService.getBalances(tripId, userId);
    }

    @GetMapping("/settlements")
    public List<SettlementResponse> getSettlements(@RequestParam String tripId) {
        String userId = UserContext.getUserId();
        return expenseService.getSettlements(tripId, userId);
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(@PathVariable String expenseId) {
        String userId = UserContext.getUserId();
        expenseService.deleteExpense(expenseId, userId);
    }
}
