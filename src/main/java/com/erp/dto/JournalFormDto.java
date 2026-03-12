package com.erp.dto;

import com.erp.domain.Journal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class JournalFormDto {

    private Long id;
    private String voucherNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    private String description;
    private Journal.JournalStatus status = Journal.JournalStatus.DRAFT;
    private List<JournalEntryDto> entries = new ArrayList<>();

    public BigDecimal getTotalDebit() {
        return entries.stream()
                .map(JournalEntryDto::getDebitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getTotalCredit() {
        return entries.stream()
                .map(JournalEntryDto::getCreditAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return getTotalDebit().compareTo(getTotalCredit()) == 0;
    }

    public static JournalFormDto fromEntity(Journal journal) {
        JournalFormDto dto = new JournalFormDto();
        dto.setId(journal.getId());
        dto.setVoucherNo(journal.getVoucherNo());
        dto.setTransactionDate(journal.getTransactionDate());
        dto.setDescription(journal.getDescription());
        dto.setStatus(journal.getStatus());

        journal.getEntries().forEach(entry -> {
            JournalEntryDto entryDto = new JournalEntryDto();
            entryDto.setId(entry.getId());
            entryDto.setAccountId(entry.getAccount().getId());
            entryDto.setDebitAmount(entry.getDebitAmount());
            entryDto.setCreditAmount(entry.getCreditAmount());
            entryDto.setMemo(entry.getMemo());
            dto.getEntries().add(entryDto);
        });

        return dto;
    }
}
