package com.erp.config;

import com.erp.domain.Account;
import com.erp.domain.Journal;
import com.erp.domain.JournalEntry;
import com.erp.repository.AccountRepository;
import com.erp.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;

    @Override
    public void run(String... args) {
        if (accountRepository.count() > 0) return;

        // 계정과목 초기 데이터
        Account cash      = accountRepository.save(Account.builder().code("1010").name("현금").type(Account.AccountType.ASSET).description("보유 현금").active(true).build());
        Account deposit    = accountRepository.save(Account.builder().code("1020").name("보통예금").type(Account.AccountType.ASSET).description("은행 보통예금").active(true).build());
        Account receivable = accountRepository.save(Account.builder().code("1100").name("매출채권").type(Account.AccountType.ASSET).description("외상 매출금").active(true).build());
        Account payable    = accountRepository.save(Account.builder().code("2010").name("매입채무").type(Account.AccountType.LIABILITY).description("외상 매입금").active(true).build());
        Account unpaid     = accountRepository.save(Account.builder().code("2020").name("미지급금").type(Account.AccountType.LIABILITY).description("미지급 비용").active(true).build());
        Account capital    = accountRepository.save(Account.builder().code("3010").name("자본금").type(Account.AccountType.EQUITY).description("납입 자본금").active(true).build());
        Account sales      = accountRepository.save(Account.builder().code("4010").name("매출").type(Account.AccountType.REVENUE).description("상품 매출").active(true).build());
        Account cogs       = accountRepository.save(Account.builder().code("5010").name("매입원가").type(Account.AccountType.EXPENSE).description("상품 매입 원가").active(true).build());
        Account salary     = accountRepository.save(Account.builder().code("5020").name("급여").type(Account.AccountType.EXPENSE).description("직원 급여").active(true).build());
        Account rent       = accountRepository.save(Account.builder().code("5030").name("임차료").type(Account.AccountType.EXPENSE).description("사무실 임대료").active(true).build());

        // 전표 테스트 데이터 10건
        LocalDate today = LocalDate.now();

        // 1. 현금 매출 발생
        createJournal("JV-" + fmt(today.minusDays(9)) + "-0001", today.minusDays(9),
                "거래처A 상품 현금 매출", Journal.JournalStatus.POSTED,
                cash, new BigDecimal("500000"), sales, new BigDecimal("500000"));

        // 2. 외상 매출 발생
        createJournal("JV-" + fmt(today.minusDays(8)) + "-0001", today.minusDays(8),
                "거래처B 외상 매출", Journal.JournalStatus.APPROVED,
                receivable, new BigDecimal("1200000"), sales, new BigDecimal("1200000"));

        // 3. 상품 매입 (현금)
        createJournal("JV-" + fmt(today.minusDays(7)) + "-0001", today.minusDays(7),
                "원자재 현금 매입", Journal.JournalStatus.POSTED,
                cogs, new BigDecimal("350000"), cash, new BigDecimal("350000"));

        // 4. 상품 매입 (외상)
        createJournal("JV-" + fmt(today.minusDays(6)) + "-0001", today.minusDays(6),
                "부품 외상 매입", Journal.JournalStatus.APPROVED,
                cogs, new BigDecimal("800000"), payable, new BigDecimal("800000"));

        // 5. 직원 급여 지급
        createJournal("JV-" + fmt(today.minusDays(5)) + "-0001", today.minusDays(5),
                "3월 직원 급여 지급", Journal.JournalStatus.POSTED,
                salary, new BigDecimal("3000000"), deposit, new BigDecimal("3000000"));

        // 6. 사무실 임차료 지급
        createJournal("JV-" + fmt(today.minusDays(4)) + "-0001", today.minusDays(4),
                "3월 사무실 임대료", Journal.JournalStatus.APPROVED,
                rent, new BigDecimal("1500000"), deposit, new BigDecimal("1500000"));

        // 7. 외상대금 회수
        createJournal("JV-" + fmt(today.minusDays(3)) + "-0001", today.minusDays(3),
                "거래처B 외상대금 일부 회수", Journal.JournalStatus.DRAFT,
                deposit, new BigDecimal("600000"), receivable, new BigDecimal("600000"));

        // 8. 매입채무 결제
        createJournal("JV-" + fmt(today.minusDays(2)) + "-0001", today.minusDays(2),
                "거래처C 매입대금 결제", Journal.JournalStatus.DRAFT,
                payable, new BigDecimal("400000"), deposit, new BigDecimal("400000"));

        // 9. 비품 구매
        createJournal("JV-" + fmt(today.minusDays(1)) + "-0001", today.minusDays(1),
                "사무용 비품 구매", Journal.JournalStatus.DRAFT,
                cogs, new BigDecimal("150000"), cash, new BigDecimal("150000"));

        // 10. 현금 매출 (당일)
        createJournal("JV-" + fmt(today) + "-0001", today,
                "거래처D 현금 매출", Journal.JournalStatus.CANCELLED,
                cash, new BigDecimal("250000"), sales, new BigDecimal("250000"));
    }

    private void createJournal(String voucherNo, LocalDate date, String desc,
                               Journal.JournalStatus status,
                               Account debitAccount, BigDecimal debitAmt,
                               Account creditAccount, BigDecimal creditAmt) {
        Journal journal = Journal.builder()
                .voucherNo(voucherNo)
                .transactionDate(date)
                .description(desc)
                .status(status)
                .totalAmount(debitAmt)
                .build();

        JournalEntry debitEntry = JournalEntry.builder()
                .account(debitAccount)
                .debitAmount(debitAmt)
                .creditAmount(BigDecimal.ZERO)
                .memo(debitAccount.getName())
                .build();
        journal.addEntry(debitEntry);

        JournalEntry creditEntry = JournalEntry.builder()
                .account(creditAccount)
                .debitAmount(BigDecimal.ZERO)
                .creditAmount(creditAmt)
                .memo(creditAccount.getName())
                .build();
        journal.addEntry(creditEntry);

        journalRepository.save(journal);
    }

    private String fmt(LocalDate date) {
        return date.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
