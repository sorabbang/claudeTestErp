package com.erp.controller;

import com.erp.domain.Account;
import com.erp.domain.Journal;
import com.erp.domain.JournalEntry;
import com.erp.repository.AccountRepository;
import com.erp.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;

    @GetMapping("/")
    public String dashboard(Model model) {
        // Summary counts
        model.addAttribute("accountCount", accountRepository.count());
        model.addAttribute("activeAccountCount", accountRepository.countByActiveTrue());
        model.addAttribute("journalCount", journalRepository.count());

        // Monthly journal count
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        model.addAttribute("monthlyJournalCount", journalRepository.countByTransactionDateBetween(monthStart, monthEnd));

        // Journal status counts
        long draftCount = journalRepository.countByStatus(Journal.JournalStatus.DRAFT);
        long approvedCount = journalRepository.countByStatus(Journal.JournalStatus.APPROVED);
        long postedCount = journalRepository.countByStatus(Journal.JournalStatus.POSTED);
        long cancelledCount = journalRepository.countByStatus(Journal.JournalStatus.CANCELLED);
        model.addAttribute("draftCount", draftCount);
        model.addAttribute("approvedCount", approvedCount);
        model.addAttribute("postedCount", postedCount);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("pendingCount", draftCount);

        // Total debit this month - 상태별 분류
        List<Journal> monthlyJournals = journalRepository.findByTransactionDateBetween(monthStart, monthEnd);

        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal approvedDebit = BigDecimal.ZERO;
        BigDecimal draftDebit = BigDecimal.ZERO;
        BigDecimal postedDebit = BigDecimal.ZERO;
        BigDecimal cancelledDebit = BigDecimal.ZERO;

        for (Journal j : monthlyJournals) {
            BigDecimal journalDebit = j.getEntries().stream()
                    .map(JournalEntry::getDebitAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalDebit = totalDebit.add(journalDebit);

            switch (j.getStatus()) {
                case DRAFT:
                    draftDebit = draftDebit.add(journalDebit);
                    break;
                case APPROVED:
                    approvedDebit = approvedDebit.add(journalDebit);
                    break;
                case POSTED:
                    postedDebit = postedDebit.add(journalDebit);
                    break;
                case CANCELLED:
                    cancelledDebit = cancelledDebit.add(journalDebit);
                    break;
            }
        }

        model.addAttribute("totalDebit", totalDebit);
        model.addAttribute("approvedDebit", approvedDebit);
        model.addAttribute("draftDebit", draftDebit);
        model.addAttribute("postedDebit", postedDebit);
        model.addAttribute("cancelledDebit", cancelledDebit);
        // 확정금액 = 승인 + 전기완료
        model.addAttribute("confirmedDebit", approvedDebit.add(postedDebit));
        // 미확정금액 = 임시저장
        model.addAttribute("unconfirmedDebit", draftDebit);

        // Recent journals
        model.addAttribute("recentJournals", journalRepository.findTop10ByOrderByCreatedAtDesc());

        // Account type distribution for pie chart
        Map<String, Long> accountTypeData = new LinkedHashMap<>();
        for (Account.AccountType type : Account.AccountType.values()) {
            accountTypeData.put(type.getLabel(), accountRepository.countByType(type));
        }
        model.addAttribute("accountTypeData", accountTypeData);

        // Monthly journal data for bar chart (last 6 months)
        Map<String, Long> monthlyJournalData = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            YearMonth ym = YearMonth.from(now).minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            long count = journalRepository.countByTransactionDateBetween(start, end);
            monthlyJournalData.put(ym.getMonthValue() + "월", count);
        }
        model.addAttribute("monthlyJournalData", monthlyJournalData);

        return "dashboard";
    }
}
