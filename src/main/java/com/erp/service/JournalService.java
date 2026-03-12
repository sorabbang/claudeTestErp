package com.erp.service;

import com.erp.domain.Account;
import com.erp.domain.Journal;
import com.erp.domain.JournalEntry;
import com.erp.dto.JournalEntryDto;
import com.erp.dto.JournalFormDto;
import com.erp.repository.AccountRepository;
import com.erp.repository.JournalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JournalService {

    private final JournalRepository journalRepository;
    private final AccountRepository accountRepository;

    public List<Journal> findAll() {
        return journalRepository.findAll();
    }

    public Journal findById(Long id) {
        return journalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("전표를 찾을 수 없습니다. ID: " + id));
    }

    public List<Journal> findByDateRange(LocalDate start, LocalDate end) {
        return journalRepository.findByTransactionDateBetween(start, end);
    }

    @Transactional
    public Journal saveFromDto(JournalFormDto dto) {
        Journal journal;

        if (dto.getId() != null) {
            // 수정: 기존 전표 로드 후 업데이트
            journal = findById(dto.getId());
            journal.setTransactionDate(dto.getTransactionDate());
            journal.setDescription(dto.getDescription());
            journal.setStatus(dto.getStatus());
            // 기존 분개 삭제 후 새로 추가
            journal.getEntries().clear();
        } else {
            // 신규
            journal = Journal.builder()
                    .voucherNo(generateVoucherNo())
                    .transactionDate(dto.getTransactionDate())
                    .description(dto.getDescription())
                    .status(dto.getStatus())
                    .totalAmount(BigDecimal.ZERO)
                    .build();
        }

        // 분개 라인 추가
        BigDecimal totalDebit = BigDecimal.ZERO;
        for (JournalEntryDto entryDto : dto.getEntries()) {
            if (entryDto.getAccountId() == null) continue;

            Account account = accountRepository.findById(entryDto.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "계정과목을 찾을 수 없습니다. ID: " + entryDto.getAccountId()));

            JournalEntry entry = JournalEntry.builder()
                    .account(account)
                    .debitAmount(entryDto.getDebitAmount())
                    .creditAmount(entryDto.getCreditAmount())
                    .memo(entryDto.getMemo())
                    .build();

            journal.addEntry(entry);
            totalDebit = totalDebit.add(entryDto.getDebitAmount());
        }

        journal.setTotalAmount(totalDebit);

        return journalRepository.save(journal);
    }

    @Transactional
    public Journal save(Journal journal) {
        if (journal.getVoucherNo() == null || journal.getVoucherNo().isEmpty()) {
            journal.setVoucherNo(generateVoucherNo());
        }
        return journalRepository.save(journal);
    }

    @Transactional
    public void delete(Long id) {
        journalRepository.deleteById(id);
    }

    @Transactional
    public Journal updateStatus(Long id, Journal.JournalStatus status) {
        Journal journal = findById(id);
        journal.setStatus(status);
        return journalRepository.save(journal);
    }

    private String generateVoucherNo() {
        String prefix = "JV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        long nextNum = journalRepository.findMaxVoucherNoByPrefix(prefix)
                .map(maxNo -> {
                    String numPart = maxNo.substring(prefix.length());
                    return Long.parseLong(numPart) + 1;
                })
                .orElse(1L);
        return prefix + String.format("%04d", nextNum);
    }
}
