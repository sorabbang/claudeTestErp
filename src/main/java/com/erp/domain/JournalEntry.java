package com.erp.domain;

import javax.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "journal_entries")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_id", nullable = false)
    private Journal journal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal debitAmount;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal creditAmount;

    @Column(length = 200)
    private String memo;
}
