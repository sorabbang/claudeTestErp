package com.erp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class JournalEntryDto {

    private Long id;
    private Long accountId;
    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String memo;

    public BigDecimal getDebitAmount() {
        return debitAmount != null ? debitAmount : BigDecimal.ZERO;
    }

    public BigDecimal getCreditAmount() {
        return creditAmount != null ? creditAmount : BigDecimal.ZERO;
    }
}
