package com.triptribe.expenseservice.service.impl;

import com.triptribe.expenseservice.client.TripServiceClient;
import com.triptribe.expenseservice.dto.*;
import com.triptribe.expenseservice.entity.*;
import com.triptribe.expenseservice.mapper.ExpenseMapper;
import com.triptribe.expenseservice.repository.ExpenseHistoryRepository;
import com.triptribe.expenseservice.repository.ExpenseParticipantRepository;
import com.triptribe.expenseservice.repository.ExpenseRepository;
import com.triptribe.expenseservice.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository participantRepository;
    private final ExpenseHistoryRepository historyRepository;
    private final TripServiceClient tripServiceClient;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse createExpense(CreateExpenseRequest request, String userId) {
        // 1. Validate Trip & Membership
        tripServiceClient.validateTripAndMember(request.getTripId(), userId);

        // 2. Validate Participants (must be > 0)
        if (request.getParticipants() == null || request.getParticipants().isEmpty()) {
            throw new IllegalArgumentException("At least one participant is required");
        }

        // 3. Create Expense Entity
        Expense expense = new Expense();
        expense.setTripId(request.getTripId());
        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setCurrency(request.getCurrency());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setSplitType(request.getSplitType());
        expense.setCategory(request.getCategory());
        expense.setCreatedByUserId(userId);

        expense = expenseRepository.save(expense);

        // 4. Calculate Splits & Save Participants
        List<ExpenseParticipant> participants = calculateSplits(expense, request.getParticipants(), userId);
        participantRepository.saveAll(participants);

        // 5. Create History
        ExpenseHistory history = new ExpenseHistory();
        history.setExpenseId(expense.getId());
        history.setAction(ExpenseHistory.ActionType.CREATED);
        history.setChangedBy(userId);
        history.setSnapshot("Expense Created: " + expense.getTitle() + " " + expense.getTotalAmount());
        historyRepository.save(history);

        return expenseMapper.toDto(expense, participants);
    }

    private List<ExpenseParticipant> calculateSplits(Expense expense, List<ExpenseParticipantDto> requestParticipants,
            String payerId) {
        // Simple logic validation: Sum of Paid must equal Total
        // Sum of Owed must equal Total

        // For MVP, we trust the `CreateExpenseRequest` structure for EXACT/PERCENTAGE
        // if provided fully.
        // However, user prompt says "EQUAL" needs calc.

        BigDecimal total = expense.getTotalAmount();
        List<ExpenseParticipant> participants = new ArrayList<>();

        // Map request dtos to entity
        // If SplitType is EQUAL, we might ignore `amountOwed` in request and calc it?
        // User blueprint:
        // Equal Split: owed = total / numParticipants

        if (expense.getSplitType() == ExpenseSplitType.EQUAL) {
            int count = requestParticipants.size();
            BigDecimal splitAmount = total.divide(BigDecimal.valueOf(count), 2, RoundingMode.DOWN);
            BigDecimal remainder = total.subtract(splitAmount.multiply(BigDecimal.valueOf(count)));

            for (int i = 0; i < count; i++) {
                ExpenseParticipantDto dto = requestParticipants.get(i);
                ExpenseParticipant p = new ExpenseParticipant();
                p.setExpenseId(expense.getId());
                p.setUserId(dto.getUserId());
                p.setAmountPaid(dto.getAmountPaid() != null ? dto.getAmountPaid() : BigDecimal.ZERO);

                // Add remainder to first person (or creator)
                if (i == 0) {
                    p.setAmountOwed(splitAmount.add(remainder));
                } else {
                    p.setAmountOwed(splitAmount);
                }
                participants.add(p);
            }
        } else {
            // EXACT OR PERCENTAGE - Assume Frontend sends correct distribution or we
            // validate
            // For now, mapping directly assuming Validated on Client or detailed validation
            // logic here.
            // Im implementing STRICT sum check.

            BigDecimal sumPaid = BigDecimal.ZERO;
            BigDecimal sumOwed = BigDecimal.ZERO;

            for (ExpenseParticipantDto dto : requestParticipants) {
                ExpenseParticipant p = new ExpenseParticipant();
                p.setExpenseId(expense.getId());
                p.setUserId(dto.getUserId());
                p.setAmountPaid(dto.getAmountPaid() != null ? dto.getAmountPaid() : BigDecimal.ZERO);
                p.setAmountOwed(dto.getAmountOwed() != null ? dto.getAmountOwed() : BigDecimal.ZERO);

                sumPaid = sumPaid.add(p.getAmountPaid());
                sumOwed = sumOwed.add(p.getAmountOwed());

                participants.add(p);
            }

            if (sumPaid.compareTo(total) != 0) {
                throw new IllegalArgumentException(
                        "Sum of Amount Paid (" + sumPaid + ") does not match Total Amount (" + total + ")");
            }
            if (sumOwed.compareTo(total) != 0) {
                throw new IllegalArgumentException(
                        "Sum of Amount Owed (" + sumOwed + ") does not match Total Amount (" + total + ")");
            }
        }

        return participants;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponse> getTripExpenses(String tripId, String userId) {
        // Validate access
        tripServiceClient.validateTripAndMember(tripId, userId);

        List<Expense> expenses = expenseRepository.findByTripId(tripId);
        // This N+1 query is bad for prod, but ok for MVP.
        // Would optimize with "JOIN FETCH" or finding all participants for these
        // expenses in one query.

        return expenses.stream().filter(e -> !e.isDeleted())
                .map(e -> {
                    List<ExpenseParticipant> parts = participantRepository.findByExpenseId(e.getId());
                    return expenseMapper.toDto(e, parts);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BalanceResponse> getBalances(String tripId, String userId) {
        tripServiceClient.validateTripAndMember(tripId, userId);

        List<Expense> expenses = expenseRepository.findByTripId(tripId);

        Map<String, BigDecimal> balances = new HashMap<>(); // UserId -> Balance

        for (Expense e : expenses) {
            if (e.isDeleted())
                continue;

            List<ExpenseParticipant> parts = participantRepository.findByExpenseId(e.getId());
            for (ExpenseParticipant p : parts) {
                // Balance = Paid - Owed
                // Paid 100, Owed 33 => Balance +67 (Owed to me)
                // Paid 0, Owed 33 => Balance -33 (I owe)
                BigDecimal net = p.getAmountPaid().subtract(p.getAmountOwed());
                balances.merge(p.getUserId(), net, BigDecimal::add);
            }
        }

        return balances.entrySet().stream()
                .map(entry -> BalanceResponse.builder()
                        .userId(entry.getKey())
                        .balance(entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettlementResponse> getSettlements(String tripId, String userId) {
        List<BalanceResponse> balances = getBalances(tripId, userId);

        // Greedy Algorithm
        // 1. Separate into Debtors (-) and Creditors (+)
        List<BalanceResponse> debtors = new ArrayList<>();
        List<BalanceResponse> creditors = new ArrayList<>();

        for (BalanceResponse b : balances) {
            if (b.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(b); // Clone or use as is
            } else if (b.getBalance().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(b);
            }
        }

        // Sort optional but good for determinism?
        // debtors.sort(Comparator.comparing(BalanceResponse::getBalance));

        List<SettlementResponse> settlements = new ArrayList<>();

        int i = 0; // debtor index
        int j = 0; // creditor index

        while (i < debtors.size() && j < creditors.size()) {
            BalanceResponse debtor = debtors.get(i);
            BalanceResponse creditor = creditors.get(j);

            BigDecimal debt = debtor.getBalance().abs(); // -50 -> 50
            BigDecimal credit = creditor.getBalance(); // 100

            BigDecimal amount = debt.min(credit); // min(50, 100) = 50

            // Record settlement
            if (amount.compareTo(BigDecimal.ZERO) > 0) {
                settlements.add(SettlementResponse.builder()
                        .fromUserId(debtor.getUserId())
                        .toUserId(creditor.getUserId())
                        .amount(amount)
                        .currency("USD") // MVP Assumption, should come from Trip context
                        .build());
            }

            // Adjust remainder
            debtor.setBalance(debtor.getBalance().add(amount)); // -50 + 50 = 0
            creditor.setBalance(creditor.getBalance().subtract(amount)); // 100 - 50 = 50

            // Move pointers
            // Use small epsilon for float comparison safety or compareTo 0
            if (debtor.getBalance().abs().compareTo(new BigDecimal("0.01")) < 0) {
                i++;
            }
            if (creditor.getBalance().abs().compareTo(new BigDecimal("0.01")) < 0) {
                j++;
            }
        }

        return settlements;
    }

    @Override
    @Transactional
    public void deleteExpense(String expenseId, String userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("Expense not found"));

        // Validate trip/user access
        tripServiceClient.validateTripAndMember(expense.getTripId(), userId);

        // Only Creator or Admin/Owner? For MVP, Creator.
        if (!expense.getCreatedByUserId().equals(userId)) {
            throw new IllegalArgumentException("Only creator can delete expense");
        }

        expense.setDeleted(true);
        expenseRepository.save(expense);

        ExpenseHistory history = new ExpenseHistory();
        history.setExpenseId(expense.getId());
        history.setAction(ExpenseHistory.ActionType.DELETED);
        history.setChangedBy(userId);
        history.setSnapshot("Expense Deleted");
        historyRepository.save(history);
    }
}
